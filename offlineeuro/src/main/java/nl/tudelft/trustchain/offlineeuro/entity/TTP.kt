package nl.tudelft.trustchain.offlineeuro.entity

import android.content.Context
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.CRSGenerator
import nl.tudelft.trustchain.offlineeuro.cryptography.GrothSahaiProof
import nl.tudelft.trustchain.offlineeuro.cryptography.PedersenCommitment
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import nl.tudelft.trustchain.offlineeuro.db.RegisteredUserManager
import nl.tudelft.trustchain.offlineeuro.db.TtpCommitmentManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TTP(
    name: String = "TTP",
    group: BilinearGroup,
    communicationProtocol: ICommunicationProtocol,
    context: Context?,
    private val registeredUserManager: RegisteredUserManager = RegisteredUserManager(context, group),
    private val commitmentManager: TtpCommitmentManager = TtpCommitmentManager(context, group),
) : Participant(communicationProtocol, name) {
    val crsMap: Map<Element, Element>

    init {
        communicationProtocol.participant = this
        this.group = group
        val generatedCRS = CRSGenerator.generateCRSMap(group)
        this.crs = generatedCRS.first
        this.crsMap = generatedCRS.second
        generateKeyPair()
    }

    /**
     * Creates a JSON-formatted request body string for initiating a Verifiable Presentation (VP) flow.
     *
     * This includes a randomly generated nonce, presentation definition ID, and input descriptor ID.
     * The resulting JSON follows the expected structure for a `vp_token` request,
     * including algorithm preferences and input constraints for EUDI PID attributes.
     *
     * @return A stringified JSON object representing the VP request body.
     */
    private fun createPostBody(): String {
        val nonce = UUID.randomUUID().toString();
        val presentationDefinitionId = UUID.randomUUID().toString();
        val inputDescriptorsId = UUID.randomUUID().toString();
        return JSONObject().apply {
            put("type", "vp_token")
            put("nonce", nonce)
            put("request_uri_method", "get")
            put("presentation_definition", JSONObject().apply {
                put("id", presentationDefinitionId)
                put("input_descriptors", JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", inputDescriptorsId)
                        put("name", "Person Identification Data (PID)")
                        put("purpose", "")
                        put("format", JSONObject().apply {
                            put("dc+sd-jwt", JSONObject().apply {
                                put("sd-jwt_alg_values", JSONArray(listOf("ES256", "ES384", "ES512")))
                                put("kb-jwt_alg_values", JSONArray(listOf("RS256", "RS384", "RS512", "ES256", "ES384", "ES512")))
                            })
                        })
                        put("constraints", JSONObject().apply {
                            put("fields", JSONArray().apply {
                                val requiredPaths = listOf(
                                    "\$.vct" to JSONObject().apply {
                                        put("type", "string")
                                        put("const", "urn:eudi:pid:1")
                                    },
                                    "\$.family_name" to null,
                                    "\$.given_name" to null,
                                )

                                requiredPaths.forEach { (path, filter) ->
                                    put(JSONObject().apply {
                                        put("path", JSONArray(listOf(path)))
                                        filter?.let { put("filter", it) }
                                        put("intent_to_retain", true)
                                    })
                                }
                            })
                        })
                    })
                })
            })
        }.toString();
    }

    /**
     * Sends an HTTP POST request to the EUDI Verifier backend to initiate a presentation flow.
     *
     * The method builds a request using the body from [createPostBody],
     * sends it using OkHttp, and extracts key fields from the server response.
     *
     * @return A map containing the keys `"transaction_id"`, `"client_id"`, `"request_uri"`, and `"request_uri_method"`.
     * @throws Exception if the HTTP response is unsuccessful or the body is null.
     */
    private fun requestPresentationEudi(): Map<String, String> {
        val requestBody = createPostBody()
        val request = Request.Builder()
            .url("https://verifier-backend.eudiw.dev/ui/presentations")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        OkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val json = response.body?.string() ?: throw Exception("Empty response")
            return mapOf(
                "transaction_id" to JSONObject(json).getString("transaction_id"),
                "client_id" to JSONObject(json).getString("client_id"),
                "request_uri" to JSONObject(json).getString("request_uri"),
                "request_uri_method" to JSONObject(json).getString("request_uri_method")
            )
        }
    }

    /**
     * Registers a new user with the verifier backend by first requesting a presentation and then storing the user.
     * The user is not marked as verified yet.
     *
     * It triggers the [requestPresentationEudi] call, retrieves the `transaction_id`, and
     * attempts to register the user in the local database via [registeredUserManager].
     *
     * @param name The display name of the user.
     * @param publicKey The cryptographic public key of the user.
     * @return A map with verifier session data if registration succeeds, or null if any part fails.
     */
    fun registerUser(
        name: String,
        publicKey: Element
    ): Map<String, String>? {
        try {
            // Sign the user's public key with TTP's private key
            val signedPublicKey = Schnorr.schnorrSignature(this.privateKey, publicKey.toBytes(), group)
            val response = requestPresentationEudi()
            val transactionId = response["transaction_id"]
            val result = registeredUserManager.addRegisteredUser(name, publicKey, signedPublicKey, transactionId!!)
            if (result)
                return response;
            return null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Verifies the status of a user's presentation session with the verifier backend.
     *
     * This checks whether the user has submitted a valid presentation by issuing a GET request
     * to the verifier endpoint using the provided `transactionId`.
     *
     * @param transactionId The identifier for the verifier presentation session.
     * @return `true` if the user submitted a valid presentation; `false` otherwise.
     */
    private fun verifyUserWallet(transactionId: String): Boolean {
        val request = Request.Builder()
            .url("https://verifier-backend.eudiw.dev/ui/presentations/$transactionId")
            .get()
            .build()
        OkHttpClient().newCall(request).execute().use { response ->
            return response.isSuccessful && response.body?.string() != null
        }
    }

    /**
     * Verifies a registered user by checking if they have completed the required wallet presentation.
     *
     * Retrieves the stored transaction ID for the given public key, then uses [verifyUserWallet]
     * to confirm their participation. If valid, marks the user as verified and triggers a UI callback.
     *
     * @param name The name of the user to verify.
     * @param publicKey The public key used to look up the registered user.
     * @return `true` if the user is verified successfully; `false` otherwise.
     */
    fun verifyUser(
        name: String,
        publicKey: Element
    ): Boolean {
        try {
            val user = registeredUserManager.getRegisteredUserByPublicKey(publicKey) ?: return false;
            val transactionId = user.transactionId
            if (verifyUserWallet(transactionId)) {
                val result = registeredUserManager.verifyUserByPublicKey(publicKey)
                emitEvent("Registered $name")
                return result
            } else {
                return false;
            }
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Retrieves the public key of an user with the TTP's signature on it.
     *
     * @param publicKey the unsigned public key of an user
     * @return the signed version of the public key
     * @throws IllegalArgumentException if the given public key is not registered
     */
//    fun getSignedUserPublicKey(
//        publicKey: Element
//    ): SchnorrSignature {
//        val registeredUser = registeredUserManager.getRegisteredUserByPublicKey(publicKey)
//            ?: throw IllegalArgumentException("User with public key $publicKey is not registered with TTP")
//
//        return registeredUser.signedPublicKey
//    }

    fun getRegisteredUsers(): List<RegisteredUser> {
        return registeredUserManager.getAllRegisteredUsers()
    }

    /**
     * Generates and stores a Pedersen commitment for a user's JWT token
     */
    fun generateAndStoreJwtCommitment(userPublicKey: Element, jwtToken: String): Element {
        val nonce = group.getRandomZr()
        val message = group.pairing.zr.newElementFromBytes(jwtToken.toByteArray())
        val commitment = PedersenCommitment.createCommitment(group, message, nonce)

        // Store the JWT and nonce in TTP's database
        commitmentManager.storeCommitment(userPublicKey, jwtToken, nonce)

        return commitment
    }

    /**
     * Reveals the JWT and nonce for a given user's public key
     */
    fun revealCommitment(userPublicKey: Element): Pair<String, Element>? {
        return commitmentManager.getCommitmentByPublicKey(userPublicKey)
    }

    override fun onReceivedTransaction(
        transactionDetails: TransactionDetails,
        publicKeyBank: Element,
        publicKeySender: Element
    ): String {
        TODO("Not yet implemented")
    }

    fun getUserFromProof(grothSahaiProof: GrothSahaiProof): RegisteredUser? {
        val crsExponent = crsMap[crs.u]
        val test = group.g.powZn(crsExponent)
        val publicKey =
            grothSahaiProof.c1.powZn(crsExponent!!.mul(-1)).mul(grothSahaiProof.c2).immutable

        return registeredUserManager.getRegisteredUserByPublicKey(publicKey)
    }

    fun getUserFromProofs(
        firstProof: GrothSahaiProof,
        secondProof: GrothSahaiProof
    ): String {
        val firstPK = getUserFromProof(firstProof)
        val secondPK = getUserFromProof(secondProof)

        return if (firstPK != null && firstPK == secondPK) {
            emitEvent("Found proof that  ${firstPK.name} committed fraud!")
            "Double spending detected. Double spender is ${firstPK.name} with PK: ${firstPK.publicKey}"
        } else {
            emitEvent("Invalid fraud request received!")
            "No double spending detected"
        }
    }

    fun isUserPublicKeyRegistered(publicKey: Element): Boolean {
        return registeredUserManager.getAllRegisteredUsers().any { it.publicKey == publicKey }
    }

    override fun reset() {
        registeredUserManager.clearAllRegisteredUsers()
        commitmentManager.clearAllCommitments()
    }

}

package nl.tudelft.trustchain.offlineeuro.entity

import android.content.Context
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.CRSGenerator
import nl.tudelft.trustchain.offlineeuro.cryptography.GrothSahaiProof
import nl.tudelft.trustchain.offlineeuro.db.RegisteredUserManager
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
    onDataChangeCallback: ((String?) -> Unit)? = null
) : Participant(communicationProtocol, name, onDataChangeCallback) {
    val crsMap: Map<Element, Element>

    init {
        communicationProtocol.participant = this
        this.group = group
        val generatedCRS = CRSGenerator.generateCRSMap(group)
        this.crs = generatedCRS.first
        this.crsMap = generatedCRS.second
        generateKeyPair()
    }

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

    fun registerUser(
        name: String,
        publicKey: Element
    ): Map<String, String>? {
        try {
            val response = requestPresentationEudi()
            val transactionId = response["transaction_id"]
            val result = registeredUserManager.addRegisteredUser(name, publicKey, transactionId!!)
            if (result)
                return response;
            return null
        } catch (e: Exception) {
            return null
        }
    }

    private fun verifyUserWallet(transactionId: String): Boolean {
        val request = Request.Builder()
            .url("https://verifier-backend.eudiw.dev/ui/presentations/$transactionId")
            .get()
            .build()
        OkHttpClient().newCall(request).execute().use { response ->
            return response.isSuccessful && response.body?.string() != null
        }
    }

    fun verifyUser(
        name: String,
        publicKey: Element
    ): Boolean {
        try {
            val user = registeredUserManager.getRegisteredUserByPublicKey(publicKey) ?: return false;
            val transactionId = user.transactionId
            if (verifyUserWallet(transactionId)) {
                val result = registeredUserManager.verifyUserByPublicKey(publicKey)
                onDataChangeCallback?.invoke("Registered $name")
                return result
            } else {
                return false;
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun getRegisteredUsers(): List<RegisteredUser> {
        return registeredUserManager.getAllRegisteredUsers()
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
            onDataChangeCallback?.invoke("Found proof that  ${firstPK.name} committed fraud!")
            "Double spending detected. Double spender is ${firstPK.name} with PK: ${firstPK.publicKey}"
        } else {
            onDataChangeCallback?.invoke("Invalid fraud request received!")
            "No double spending detected"
        }
    }

    override fun reset() {
        registeredUserManager.clearAllRegisteredUsers()
    }
}

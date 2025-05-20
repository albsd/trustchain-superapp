package nl.tudelft.trustchain.offlineeuro.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import androidx.core.content.edit

class HomeFragment : OfflineEuroBaseFragment(R.layout.fragment_home) {
    private val client = OkHttpClient()
    private var transactionId: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"

        // Keep all existing button listeners
        view.findViewById<Button>(R.id.JoinAsTTP).setOnClickListener {
            findNavController().navigate(R.id.nav_home_ttphome)
        }

        view.findViewById<Button>(R.id.JoinAsBankButton).setOnClickListener {
            findNavController().navigate(R.id.nav_home_bankhome)
        }

        view.findViewById<Button>(R.id.JoinAsUserButton).setOnClickListener {
            showAlertDialog()
        }

        view.findViewById<Button>(R.id.JoinAsAllRolesButton).setOnClickListener {
            findNavController().navigate(R.id.nav_home_all_roles_home)
        }

        view.findViewById<Button>(R.id.OpenInWallet).setOnClickListener {
            lifecycleScope.launch {
                try {
                    val nonce = UUID.randomUUID().toString()
                    val requestBody = createPostBody(nonce)
                    val response = makeNetworkRequest(requestBody)
                    launchWalletIntent(response)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

//        activity?.intent?.data?.let { uri ->

            val prefs = context?.getSharedPreferences("offline_euro", Context.MODE_PRIVATE)
            transactionId = prefs?.getString("transaction_id", null)
            Log.d("Application has returned", transactionId!!)
//            if (uri.scheme == "offlineeuro" && uri.host == "wallet_response") {
                if (transactionId != null) {
                    lifecycleScope.launch {
                        try {
                            getWalletDetails(transactionId!!)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Could not record identity!", Toast.LENGTH_LONG).show()
                }
//            }
//        }
    }

    private fun createPostBody(nonce: String): String {
        return JSONObject().apply {
            put("type", "vp_token")
            put("nonce", nonce)
            put("request_uri_method", "get")
//            put("wallet_response_redirect_uri_template", "https://appurl.io/ACns8QEcLZ?response_code={RESPONSE_CODE}")
            put("presentation_definition", JSONObject().apply {
                put("id", "albertino")
                put("input_descriptors", JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", "crocodillio")
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
                                // Field definitions
                                put(JSONObject().apply {
                                    put("path", JSONArray(listOf("\$.vct")))
                                    put("filter", JSONObject().apply {
                                        put("type", "string")
                                        put("const", "urn:eu.europa.ec.eudi:pid:1")
                                    })
                                })
                                // Add other fields similarly...
                                put(JSONObject().apply {
                                    put("path", JSONArray(listOf("\$.family_name")))
                                    put("intent_to_retain", false)
                                })
                                // ... Add all your remaining fields here ...
                            })
                        })
                    })
                })
            })
        }.toString()
    }

    private suspend fun makeNetworkRequest(body: String): Map<String, String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://verifier-backend.eudiw.dev/ui/presentations")
            .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val json = response.body?.string() ?: throw Exception("Empty response")
            return@use mapOf(
                "transaction_id" to JSONObject(json).getString("transaction_id"),
                "client_id" to JSONObject(json).getString("client_id"),
                "request_uri" to JSONObject(json).getString("request_uri"),
                "request_uri_method" to JSONObject(json).getString("request_uri_method")
            )
        }
    }

    private suspend fun getWalletDetails(transactionId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://verifier-backend.eudiw.dev/ui/presentations/$transactionId")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val json = response.body?.string() ?: throw Exception("Empty response")
            Log.d("transaction details", json)
            return@use mapOf()
        }
    }

    private fun launchWalletIntent(response: Map<String, String>) {
        Log.d( "client_id: " ,  "" + response["client_id"])
        Log.d( "request_uri: " ,  "" + response["request_uri"])
        Log.d("transaction_id: ", "" + response["transaction_id"])
        val prefs = context?.getSharedPreferences("offline_euro", Context.MODE_PRIVATE)
        transactionId = response["transaction_id"]
        prefs?.edit(commit = true) { putString("transaction_id", transactionId!!) }

        // https://verifier.eudiw.dev/get-wallet-code?response_code=3752894f-3bea-4455-b6e2-3958459499c8
        // https://verifier-backend.eudiw.dev/wallet/request.jwt/KHPI0d7u5hcBGh0sOhcb4-Q-gGJlXdOgfZryVuPvlSVS7DYjV1nLuMom_LXoecLDFt1rLUAJJZ7a-7tPa2lOKw
        Log.d( "request_uri_method: " ,  "" + response["request_uri_method"])
        val deepLink = Uri.parse("eudi-openid4vp://").buildUpon()
            .appendQueryParameter("client_id", response["client_id"])
            .appendQueryParameter("request_uri", response["request_uri"])
            .appendQueryParameter("request_uri_method", response["request_uri_method"])
            .build()

        try {
            startActivity(Intent(Intent.ACTION_VIEW, deepLink).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No wallet app found", Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        try {
            val euroTokenCommunity = getIpv8().getOverlay<OfflineEuroCommunity>()
            if (euroTokenCommunity == null) {
                Toast.makeText(requireContext(), "Could not find community", Toast.LENGTH_LONG)
                    .show()
            }
            if (euroTokenCommunity != null) {
                Toast.makeText(requireContext(), "Found community", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            logger.error { e }
            Toast.makeText(
                requireContext(),
                "Failed to send transactions",
                Toast.LENGTH_LONG
            )
                .show()
        }
        return
    }

    private fun showAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        val editText = EditText(requireContext())
        alertDialogBuilder.setView(editText)
        alertDialogBuilder.setTitle("Pick an username")
        alertDialogBuilder.setMessage("")
        // Set positive button
        alertDialogBuilder.setPositiveButton("Join!") { dialog, which ->
            val username = editText.text.toString()
            moveToUserHome(username)
        }

        // Set negative button
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        // Create and show the AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun moveToUserHome(userName: String) {
        val bundle = bundleOf("userName" to userName)
        findNavController().navigate(R.id.nav_home_userhome, bundle)
    }
}

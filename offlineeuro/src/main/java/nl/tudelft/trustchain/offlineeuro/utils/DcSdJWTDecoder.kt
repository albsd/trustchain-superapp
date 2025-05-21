package nl.tudelft.trustchain.offlineeuro.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64

class DcSdJWTDecoder {
    companion object {
        private fun base64UrlDecode(input: String): ByteArray {
            val padding = 4 - (input.length % 4)
            val adjusted = input + "=".repeat(if (padding in 1..3) padding else 0)
            return Base64.getUrlDecoder().decode(adjusted)
        }

        fun decodeSdJwt(dcSdJwt: String): Map<String, String> {
            val parts = dcSdJwt.split("~")
            val sdJwt = parts[0]
            val disclosures = parts.subList(1, parts.size - 1)
            val holderBindingJwt = parts.lastOrNull()

            Log.d("JWT", "=== SD-JWT Payload ===")
            val sdJwtPayload = sdJwt.split(".")[1]
            val decodedPayload = String(base64UrlDecode(sdJwtPayload))
            Log.d("JWT", JSONObject(decodedPayload).toString(2))

            Log.d("JWT", "\n=== Disclosures ===")
            val claimsMap = mutableMapOf<String, String>()
            disclosures.forEachIndexed { idx, disclosure ->
                try {
                    val decoded = String(base64UrlDecode(disclosure))
                    val jsonArray = JSONArray(decoded)
                    if (jsonArray.length() >= 3) {
                        val key = jsonArray.getString(1)
                        val value = jsonArray.getString(2)
                        claimsMap[key] = value
                    }
                    Log.d("JWT", "Disclosure $idx: $jsonArray")
                } catch (e: Exception) {
                    Log.d("JWT", "Failed to decode disclosure $idx: $e")
                }
            }

            if (holderBindingJwt != null && holderBindingJwt.contains(".")) {
                Log.d("JWT", "\n=== Holder Binding JWT Payload ===")
                val holderPayload = holderBindingJwt.split(".")[1]
                val decodedHolderPayload = String(base64UrlDecode(holderPayload))
                Log.d("JWT", JSONObject(decodedHolderPayload).toString(2))
            }
            return claimsMap;
        }
    }
}

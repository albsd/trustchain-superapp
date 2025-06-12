package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import kotlinx.serialization.json.*
import java.util.Base64
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class FraudControlReplyPayload(
    val serialNumber: String,
    val isFraud : Boolean,
    val jwtPlaintext: String?,
    val noncePlaintext : ByteArray?,
    val userName: String?,
    val pkBytes: ByteArray?
) : Serializable {

    override fun serialize(): ByteArray {
        val json = Json
        val jsonObject = buildJsonObject {
            put("serialNumber", serialNumber)
            put("isFraud", isFraud)
            put("jwt", jwtPlaintext?.let { JsonPrimitive(it) } ?: JsonNull)
            put("nonce", noncePlaintext?.let { JsonPrimitive(Base64.getEncoder().encodeToString(it)) } ?: JsonNull)
            put("userName", userName?.let { JsonPrimitive(it) } ?: JsonNull)
            put("userPK", pkBytes?.let { JsonPrimitive(Base64.getEncoder().encodeToString(it)) } ?: JsonNull)
        }
        val encodedJson = json.encodeToString(JsonObject.serializer(), jsonObject).encodeToByteArray()
        return serializeVarLen(encodedJson)
    }

    companion object Deserializer : Deserializable<FraudControlReplyPayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<FraudControlReplyPayload, Int> {
            var localOffset = offset

            val json = Json
            val (jsonBuffer, size) = deserializeVarLen(buffer, localOffset)
            localOffset += size

            val jsonString = jsonBuffer.decodeToString()
            val jsonObject = json.decodeFromString(JsonObject.serializer(), jsonString)

            val serialNumber = jsonObject["serialNumber"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing serialNumber")
            val isFraud = jsonObject["isFraud"]?.jsonPrimitive?.boolean
                ?: throw IllegalArgumentException("Missing isFraud")
            val jwt = jsonObject["jwt"]?.jsonPrimitive?.contentOrNull
            val nonce = jsonObject["nonce"]?.jsonPrimitive?.contentOrNull?.let { Base64.getDecoder().decode(it) }
            val userName = jsonObject["userName"]?.jsonPrimitive?.contentOrNull
            val pkBytes = jsonObject["userPK"]?.jsonPrimitive?.contentOrNull?.let {
                Base64.getDecoder().decode(it)
            }
            return Pair(
                FraudControlReplyPayload(
                    serialNumber,
                    isFraud,
                    jwt,
                    nonce,
                    userName,
                    pkBytes,
                ),
                localOffset - offset
            )
        }
    }
}

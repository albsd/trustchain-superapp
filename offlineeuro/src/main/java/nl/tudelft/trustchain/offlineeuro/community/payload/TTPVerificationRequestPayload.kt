package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class TTPVerificationRequestPayload (
    val clientId: String,
    val requestUri: String,
    val requestUriMethod: String,
): Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(clientId.toByteArray())
        payload += serializeVarLen(requestUri.toByteArray())
        payload += serializeVarLen(requestUriMethod.toByteArray())
        return payload
    }

    companion object Deserializer : Deserializable<TTPVerificationRequestPayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPVerificationRequestPayload, Int> {
            var localOffset = offset

            val (clientIdBytes, clientIdize) = deserializeVarLen(buffer, localOffset)
            localOffset += clientIdize

            val (requestUriBytes, requestUriSize) = deserializeVarLen(buffer, localOffset)
            localOffset += requestUriSize

            val (requestUriMethodBytes, requestUriMethodSize) = deserializeVarLen(buffer, localOffset)
            localOffset += requestUriMethodSize

            return Pair(
                TTPVerificationRequestPayload(
                    clientIdBytes.toString(Charsets.UTF_8),
                    requestUriBytes.toString(Charsets.UTF_8),
                    requestUriMethodBytes.toString(Charsets.UTF_8),
                ),
                localOffset - offset
            )
        }
    }
}

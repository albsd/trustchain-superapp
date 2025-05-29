package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class TTPRegistrationCompletePayload (
    val status: String,
) : Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(status.toByteArray())
        return payload
    }

    companion object Deserializer : Deserializable<TTPRegistrationCompletePayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPRegistrationCompletePayload, Int> {
            var localOffset = offset

            val (statusBytes, statusSize) = deserializeVarLen(buffer, localOffset)
            localOffset += statusSize

            return Pair(
                TTPRegistrationCompletePayload(
                    statusBytes.toString(Charsets.UTF_8),
                ),
                localOffset - offset
            )
        }
    }
}


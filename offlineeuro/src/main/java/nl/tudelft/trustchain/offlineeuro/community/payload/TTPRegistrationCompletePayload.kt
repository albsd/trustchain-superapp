package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class TTPRegistrationCompletePayload (
    val status: String,
    val signedPK: ByteArray
) : Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(status.toByteArray())
        payload += serializeVarLen(signedPK)
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

            val (signedPK, pkSize) = deserializeVarLen(buffer, localOffset)
            localOffset += pkSize

            return Pair(
                TTPRegistrationCompletePayload(
                    statusBytes.toString(Charsets.UTF_8),
                    signedPK
                ),
                localOffset - offset
            )
        }
    }
}


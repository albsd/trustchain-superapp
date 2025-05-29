package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class TTPVerificationCompletePayload (
    val status: String,
    val userName: String,
    val publicKey: ByteArray,
): Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(status.toByteArray())
        payload += serializeVarLen(userName.toByteArray())
        payload += serializeVarLen(publicKey)
        return payload
    }

    companion object Deserializer : Deserializable<TTPVerificationCompletePayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPVerificationCompletePayload, Int> {
            var localOffset = offset

            val (statusBytes, statusSize) = deserializeVarLen(buffer, localOffset)
            localOffset += statusSize

            val (nameBytes, nameSize) = deserializeVarLen(buffer, localOffset)
            localOffset += nameSize

            val (publicKeyBytes, publicKeyBytesSize) = deserializeVarLen(buffer, localOffset)
            localOffset += publicKeyBytesSize

            return Pair(
                TTPVerificationCompletePayload(
                    statusBytes.toString(Charsets.UTF_8),
                    nameBytes.toString(Charsets.UTF_8),
                    publicKeyBytes
                ),
                localOffset - offset
            )
        }
    }
}

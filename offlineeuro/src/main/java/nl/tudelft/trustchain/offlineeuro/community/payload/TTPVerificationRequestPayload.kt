package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class TTPVerificationRequestPayload (
    val verifierLink: String,
): Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(verifierLink.toByteArray())
        return payload
    }

    companion object Deserializer : Deserializable<TTPVerificationRequestPayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPVerificationRequestPayload, Int> {
            var localOffset = offset

            val (linkBytes, linkSize) = deserializeVarLen(buffer, localOffset)
            localOffset += linkSize

            return Pair(
                TTPVerificationRequestPayload(
                    linkBytes.toString(Charsets.UTF_8),
                ),
                localOffset - offset
            )
        }
    }
}

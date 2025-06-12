package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class TTPCommitmentMessagePayload(
    val commitmentBytes: ByteArray
) : Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(commitmentBytes)
        return payload
    }

    companion object Deserializer : Deserializable<TTPCommitmentMessagePayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPCommitmentMessagePayload, Int> {
            var localOffset = offset

            val (commitmentBytes, commitmentBytesSize) = deserializeVarLen(buffer, localOffset)
            localOffset += commitmentBytesSize

            return Pair(
                TTPCommitmentMessagePayload(commitmentBytes),
                localOffset - offset
            )
        }
    }
}


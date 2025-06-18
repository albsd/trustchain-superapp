package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable
import nl.tudelft.ipv8.messaging.deserializeVarLen
import nl.tudelft.ipv8.messaging.serializeVarLen

class FraudControlRequestPayload(
    val serialNumber: String,
    val firstProofBytes: ByteArray,
    val secondProofBytes: ByteArray
) : Serializable {
    override fun serialize(): ByteArray {
        var payload = ByteArray(0)
        payload += serializeVarLen(serialNumber.toByteArray())
        payload += serializeVarLen(firstProofBytes)
        payload += serializeVarLen(secondProofBytes)
        return payload
    }

    companion object Deserializer : Deserializable<FraudControlRequestPayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<FraudControlRequestPayload, Int> {
            var localOffset = offset

            val (serialNumberBytes, serialNumberSize) = deserializeVarLen(buffer, localOffset)
            localOffset += serialNumberSize

            val (firstProofBytes, firstProofSize) = deserializeVarLen(buffer, localOffset)
            localOffset += firstProofSize

            val (secondProofBytes, secondProofSize) = deserializeVarLen(buffer, localOffset)
            localOffset += secondProofSize

            return Pair(
                FraudControlRequestPayload(serialNumberBytes.toString(Charsets.UTF_8), firstProofBytes, secondProofBytes),
                localOffset - offset
            )
        }
    }
}

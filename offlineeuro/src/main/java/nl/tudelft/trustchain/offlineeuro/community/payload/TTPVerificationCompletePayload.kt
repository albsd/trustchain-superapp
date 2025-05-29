package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable

class TTPVerificationCompletePayload : Serializable {
    override fun serialize(): ByteArray {
    }

    companion object Deserializer : Deserializable<TTPVerificationCompletePayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPVerificationCompletePayload, Int> {
        }
    }
}

package nl.tudelft.trustchain.offlineeuro.community.payload

import nl.tudelft.ipv8.messaging.Deserializable
import nl.tudelft.ipv8.messaging.Serializable

class TTPVerificationRequestPayload : Serializable {
    override fun serialize(): ByteArray {
    }

    companion object Deserializer : Deserializable<TTPVerificationRequestPayload> {
        override fun deserialize(
            buffer: ByteArray,
            offset: Int
        ): Pair<TTPVerificationRequestPayload, Int> {
        }
    }
}

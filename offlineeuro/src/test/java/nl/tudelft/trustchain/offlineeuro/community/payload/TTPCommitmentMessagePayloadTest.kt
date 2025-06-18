package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert.*
import org.junit.Test

class TTPCommitmentMessagePayloadTest {

    @Test
    fun serializeAndDeserialize_regularPayload() {
        val commitment = "SomeCommitmentBytes".toByteArray()

        val originalPayload = TTPCommitmentMessagePayload(commitment)
        val serialized = originalPayload.serialize()
        val (deserializedPayload, _) = TTPCommitmentMessagePayload.deserialize(serialized)

        assertArrayEquals("Commitment bytes should match", commitment, deserializedPayload.commitmentBytes)
    }

    @Test
    fun serializeAndDeserialize_emptyCommitment() {
        val commitment = ByteArray(0)

        val originalPayload = TTPCommitmentMessagePayload(commitment)
        val serialized = originalPayload.serialize()
        val (deserializedPayload, _) = TTPCommitmentMessagePayload.deserialize(serialized)

        assertArrayEquals("Empty commitment bytes should match", commitment, deserializedPayload.commitmentBytes)
    }

    @Test
    fun serializeAndDeserialize_largeCommitment() {
        val commitment = ByteArray(4096) { it.toByte() }

        val originalPayload = TTPCommitmentMessagePayload(commitment)
        val serialized = originalPayload.serialize()
        val (deserializedPayload, _) = TTPCommitmentMessagePayload.deserialize(serialized)

        assertArrayEquals("Large commitment bytes should match", commitment, deserializedPayload.commitmentBytes)
    }

    @Test
    fun deserialize_withOffset() {
        val commitment = "OffsetPayload".toByteArray()

        val payload = TTPCommitmentMessagePayload(commitment)
        val serialized = payload.serialize()

        val prefix = byteArrayOf(1, 2, 3, 4)
        val prefixedPayload = prefix + serialized

        val (deserializedPayload, bytesRead) = TTPCommitmentMessagePayload.deserialize(prefixedPayload, prefix.size)

        assertEquals("Bytes read should match payload size", serialized.size, bytesRead)
        assertArrayEquals("Commitment bytes should match", commitment, deserializedPayload.commitmentBytes)
    }
}

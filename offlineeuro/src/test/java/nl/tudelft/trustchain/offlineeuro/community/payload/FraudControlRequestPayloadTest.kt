package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert.*
import org.junit.Test

class FraudControlRequestPayloadTest {

    @Test
    fun serializeAndDeserialize_regularPayload() {
        val serialNumber = "serial"
        val firstProof = "firstProofData".toByteArray()
        val secondProof = "secondProofData".toByteArray()

        val payload = FraudControlRequestPayload(serialNumber, firstProof, secondProof)
        val serialized = payload.serialize()
        val (deserialized, _) = FraudControlRequestPayload.deserialize(serialized)

        assertEquals(serialNumber, deserialized.serialNumber)
        assertArrayEquals("First proof bytes should match", firstProof, deserialized.firstProofBytes)
        assertArrayEquals("Second proof bytes should match", secondProof, deserialized.secondProofBytes)
    }

    @Test
    fun serializeAndDeserialize_emptyProofs() {
        val serialNumber = "serial"
        val firstProof = ByteArray(0)
        val secondProof = ByteArray(0)

        val payload = FraudControlRequestPayload(serialNumber, firstProof, secondProof)
        val serialized = payload.serialize()
        val (deserialized, _) = FraudControlRequestPayload.deserialize(serialized)

        assertEquals(serialNumber, deserialized.serialNumber)
        assertArrayEquals("First proof should be empty", firstProof, deserialized.firstProofBytes)
        assertArrayEquals("Second proof should be empty", secondProof, deserialized.secondProofBytes)
    }

    @Test
    fun serializeAndDeserialize_largeProofs() {
        val serialNumber = "serial"
        val firstProof = ByteArray(1024) { it.toByte() }
        val secondProof = ByteArray(2048) { (it % 256).toByte() }

        val payload = FraudControlRequestPayload(serialNumber, firstProof, secondProof)
        val serialized = payload.serialize()
        val (deserialized, _) = FraudControlRequestPayload.deserialize(serialized)

        assertEquals(serialNumber, deserialized.serialNumber)
        assertArrayEquals("Large first proof bytes should match", firstProof, deserialized.firstProofBytes)
        assertArrayEquals("Large second proof bytes should match", secondProof, deserialized.secondProofBytes)
    }

    @Test
    fun deserialize_withOffset() {
        val serialNumber = "serial"
        val firstProof = "A".repeat(10).toByteArray()
        val secondProof = "B".repeat(20).toByteArray()

        val payload = FraudControlRequestPayload(serialNumber, firstProof, secondProof)
        val serialized = payload.serialize()

        // Add padding before real payload
        val prefix = byteArrayOf(99, 88, 77)
        val prefixedPayload = prefix + serialized

        val (deserialized, bytesRead) = FraudControlRequestPayload.deserialize(prefixedPayload, prefix.size)

        assertEquals(serialNumber, deserialized.serialNumber)
        assertEquals("Bytes read should match payload length", serialized.size, bytesRead)
        assertArrayEquals(firstProof, deserialized.firstProofBytes)
        assertArrayEquals(secondProof, deserialized.secondProofBytes)
    }
}

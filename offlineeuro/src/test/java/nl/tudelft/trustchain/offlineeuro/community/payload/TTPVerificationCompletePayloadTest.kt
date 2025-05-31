package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class TTPVerificationCompletePayloadTest {

    @Test
    fun serializeAndDeserializeTest() {
        val userName = "VerifiedUser42"
        val publicKeyBytes = "FakePublicKeyBytesForTesting".toByteArray()

        val originalPayload = TTPVerificationCompletePayload(userName, publicKeyBytes)
        val serialized = originalPayload.serialize()
        val (deserializedPayload, _) = TTPVerificationCompletePayload.deserialize(serialized)

        val deserializedUserName = deserializedPayload.userName
        val deserializedPublicKey = deserializedPayload.publicKey

        assertEquals("The username should match after deserialization", userName, deserializedUserName)
        assertArrayEquals("The public key bytes should match", publicKeyBytes, deserializedPublicKey)
    }
}

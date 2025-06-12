package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class TTPRegistrationCompletePayloadTest {

    @Test
    fun serializeAndDeserializeTest() {
        val status = "Completed"
        val publicKeyBytes = "NotAPublicKeyButJustSomeBytes".toByteArray()

        val serializedPayload = TTPRegistrationCompletePayload(status, publicKeyBytes).serialize()
        val (deserializedPayload, _) = TTPRegistrationCompletePayload.deserialize(serializedPayload)

        val deserializedStatus = deserializedPayload.status
        val deserializedBytes = deserializedPayload.signedPK

        assertEquals("The status should be equal", status, deserializedStatus)
        Assert.assertArrayEquals("The public key bytes should be equal", publicKeyBytes, deserializedBytes)
    }
}

package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert.assertEquals
import org.junit.Test

class TTPRegistrationCompletePayloadTest {

    @Test
    fun serializeAndDeserializeTest() {
        val status = "Completed"

        val serializedPayload = TTPRegistrationCompletePayload(status).serialize()
        val (deserializedPayload, _) = TTPRegistrationCompletePayload.deserialize(serializedPayload)

        val deserializedStatus = deserializedPayload.status

        assertEquals("The status should be equal", status, deserializedStatus)
    }
}

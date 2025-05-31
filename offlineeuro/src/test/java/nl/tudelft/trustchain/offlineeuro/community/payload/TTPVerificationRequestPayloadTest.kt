package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert.assertEquals
import org.junit.Test

class TTPVerificationRequestPayloadTest {

    @Test
    fun serializeAndDeserializeTest() {
        val clientId = "client-id-123"
        val requestUri = "https://example.com/request"
        val requestUriMethod = "get"

        val originalPayload = TTPVerificationRequestPayload(clientId, requestUri, requestUriMethod)
        val serialized = originalPayload.serialize()
        val (deserializedPayload, _) = TTPVerificationRequestPayload.deserialize(serialized)

        val deserializedClientId = deserializedPayload.clientId
        val deserializedRequestUri = deserializedPayload.requestUri
        val deserializedRequestUriMethod = deserializedPayload.requestUriMethod

        assertEquals("The clientId should match", clientId, deserializedClientId)
        assertEquals("The requestUri should match", requestUri, deserializedRequestUri)
        assertEquals("The requestUriMethod should match", requestUriMethod, deserializedRequestUriMethod)
    }
}

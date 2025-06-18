package nl.tudelft.trustchain.offlineeuro.community.payload

import org.junit.Assert.*
import org.junit.Test

class FraudControlReplyPayloadTest {

    @Test
    fun serializeAndDeserialize_fullPayload() {
        val serialNumber = "serial"
        val isFraud = true
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        val nonce = "random_nonce_value".toByteArray()
        val userName = "TestUser"
        val pkBytes = "TestPublicKeyBytes".toByteArray()

        val originalPayload = FraudControlReplyPayload(serialNumber, isFraud, jwt, nonce, userName, pkBytes)
        val serialized = originalPayload.serialize()
        val (deserialized, _) = FraudControlReplyPayload.deserialize(serialized)

        assertEquals(serialNumber, deserialized.serialNumber)
        assertEquals(isFraud, deserialized.isFraud)
        assertEquals(jwt, deserialized.jwtPlaintext)
        assertArrayEquals(nonce, deserialized.noncePlaintext)
        assertEquals(userName, deserialized.userName)
        assertArrayEquals(pkBytes, deserialized.pkBytes)
    }

    @Test
    fun serializeAndDeserialize_withNulls() {
        val isFraud = false
        val originalPayload = FraudControlReplyPayload("serial", isFraud, null, null, null, null)
        val serialized = originalPayload.serialize()
        val (deserialized, _) = FraudControlReplyPayload.deserialize(serialized)

        assertEquals("serial", deserialized.serialNumber)
        assertEquals(isFraud, deserialized.isFraud)
        assertNull(deserialized.jwtPlaintext)
        assertNull(deserialized.noncePlaintext)
        assertNull(deserialized.userName)
        assertNull(deserialized.pkBytes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun deserialize_missingIsFraud_throwsException() {
        val malformedJson = """{
            "sn": "sn",
            "jwt": "token",
            "nonce": "bm9uY2U=",
            "userName": "User",
            "userPK": "cHVibGljS2V5"
        }"""
        val encoded = malformedJson.toByteArray()
        val lengthPrefix = byteArrayOf(encoded.size.toByte())

        val buffer = lengthPrefix + encoded
        FraudControlReplyPayload.deserialize(buffer)
    }

    @Test
    fun serializeAndDeserialize_emptyStrings() {
        val payload = FraudControlReplyPayload("", true, "", ByteArray(0), "", ByteArray(0))
        val serialized = payload.serialize()
        val (deserialized, _) = FraudControlReplyPayload.deserialize(serialized)

        assertEquals("", deserialized.serialNumber)
        assertEquals(true, deserialized.isFraud)
        assertEquals("", deserialized.jwtPlaintext)
        assertArrayEquals(ByteArray(0), deserialized.noncePlaintext)
        assertEquals("", deserialized.userName)
        assertArrayEquals(ByteArray(0), deserialized.pkBytes)
    }
}

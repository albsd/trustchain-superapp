package nl.tudelft.trustchain.offlineeuro.libraries

import android.util.Log
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any

class DcSdJWTDecoderTest {
    private lateinit var mockedLog: MockedStatic<Log>

    private val exampleJwt = """
        eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2Y3QiOiJ1cm46ZXVkaTpwaWQ6MSIsInNhbHQiOiJhYmNkZWYiLCJfc2RfYWxnIjoic2hhLTI1NiJ9.~WyIxMjM0NTYiLCJmYW1pbHlfbmFtZSIsIkRvZSJd~WyI3ODkwMTIiLCJnaXZlbl9uYW1lIiwiSm9obiJd~WyIzNDU2NzgiLCJiaXJ0aF9kYXRlIiwiMTk5MC0wMS0wMSJd~eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJzdWIiOiIxMjM0NTY3ODkwIn0.signature
    """.trimIndent()

    private val minimalJwt = """
        eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2Y3QiOiJ1cm46ZXVkaTpwaWQ6MSIsInNhbHQiOiJhYmNkZWYiLCJfc2RfYWxnIjoic2hhLTI1NiJ9.~WyIxMjM0NTYiLCJmYW1pbHlfbmFtZSIsIkRvZSJd~eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJzdWIiOiIxMjM0NTY3ODkwIn0.signature
    """.trimIndent()

    private val noDisclosuresJwt = """
        eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2Y3QiOiJ1cm46ZXVkaTpwaWQ6MSIsInNhbHQiOiJhYmNkZWYiLCJfc2RfYWxnIjoic2hhLTI1NiJ9.~eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJzdWIiOiIxMjM0NTY3ODkwIn0.signature
    """.trimIndent()

    @Before
    fun setup() {
        mockedLog = mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.i(any(), any()) }.thenReturn(0)
    }

    @After
    fun tearDown() {
        mockedLog.close()
    }

    @Test
    fun testDecodeFullJwt() {
        val claims = DcSdJWTDecoder.decodeSdJwt(exampleJwt)

        assertEquals("Doe", claims["family_name"])
        assertEquals("John", claims["given_name"])
        assertEquals("1990-01-01", claims["birth_date"])
        assertEquals(3, claims.size)
    }

    @Test
    fun testDecodeMinimalJwt() {
        val claims = DcSdJWTDecoder.decodeSdJwt(minimalJwt)

        assertEquals("Doe", claims["family_name"])
        assertEquals(1, claims.size)
    }

    @Test
    fun testDecodeJwtWithNoDisclosures() {
        val claims = DcSdJWTDecoder.decodeSdJwt(noDisclosuresJwt)

        assertTrue(claims.isEmpty())
    }

    @Test
    fun testDecodeJwtWithInvalidDisclosure() {
        val invalidJwt = """
            eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2Y3QiOiJ1cm46ZXVkaTpwaWQ6MSIsInNhbHQiOiJhYmNkZWYiLCJfc2RfYWxnIjoic2hhLTI1NiJ9.~WyJpbnZhbGlkX2Rpc2Nsb3N1cmUiXX4=~eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJzdWIiOiIxMjM0NTY3ODkwIn0.signature
        """.trimIndent()

        val claims = DcSdJWTDecoder.decodeSdJwt(invalidJwt)
        assertTrue(claims.isEmpty())
    }

    @Test
    fun testDecodeJwtWithMalformedDisclosure() {
        val malformedJwt = """
            eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2Y3QiOiJ1cm46ZXVkaTpwaWQ6MSIsInNhbHQiOiJhYmNkZWYiLCJfc2RfYWxnIjoic2hhLTI1NiJ9.~WyJpbnZhbGlkX2Rpc2Nsb3N1cmUiXX4=~eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL2V4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyLCJzdWIiOiIxMjM0NTY3ODkwIn0.signature
        """.trimIndent()

        val claims = DcSdJWTDecoder.decodeSdJwt(malformedJwt)
        assertTrue(claims.isEmpty())
    }
}

package nl.tudelft.trustchain.offlineeuro.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import io.mockk.every
import io.mockk.mockk
import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class TtpCommitmentManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockDriver: SqlDriver
    private lateinit var mockPairing: Pairing
    private lateinit var mockGroup: BilinearGroup
    private lateinit var database: Database
    private lateinit var manager: TtpCommitmentManager

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockDriver = mockk(relaxed = true)
        mockPairing = mockk(relaxed = true)
        mockGroup = mockk(relaxed = true)

        every { mockGroup.pairing } returns mockPairing
        database = Database(mockDriver)
        manager = TtpCommitmentManager(mockContext, mockGroup, mockDriver)
    }

    @Test
    fun `store and retrieve commitment`() {
        // Mock elements
        val mockPublicKey = mockk<Element>()
        val mockNonce = mockk<Element>()
        val jwtToken = "test.jwt.token"
        val publicKeyBytes = "publicKey".toByteArray()
        val nonceBytes = "nonce".toByteArray()

        every { mockPublicKey.toBytes() } returns publicKeyBytes
        every { mockNonce.toBytes() } returns nonceBytes
        every { mockPairing.zr.newElementFromBytes(nonceBytes) } returns mockNonce

        // Test storage
        manager.storeCommitment(mockPublicKey, jwtToken, mockNonce)

        // Test retrieval
        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertNotNull(result)
        assertEquals(jwtToken, result!!.first)
        assertEquals(mockNonce, result.second)
    }

    @Test
    fun `get non-existent commitment returns null`() {
        val mockPublicKey = mockk<Element>()
        every { mockPublicKey.toBytes() } returns "nonexistent".toByteArray()

        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertNull(result)
    }

    @Test
    fun `clear all commitments`() {
        val mockPublicKey = mockk<Element>()
        val mockNonce = mockk<Element>()
        every { mockPublicKey.toBytes() } returns "key".toByteArray()
        every { mockNonce.toBytes() } returns "nonce".toByteArray()
        every { mockPairing.zr.newElementFromBytes(any()) } returns mockNonce

        manager.storeCommitment(mockPublicKey, "token", mockNonce)
        manager.clearAllCommitments()

        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertNull(result)
    }
}

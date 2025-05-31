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

class BankCommitmentManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockDriver: SqlDriver
    private lateinit var mockPairing: Pairing
    private lateinit var mockGroup: BilinearGroup
    private lateinit var database: Database
    private lateinit var manager: BankCommitmentManager

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockDriver = mockk(relaxed = true)
        mockPairing = mockk(relaxed = true)
        mockGroup = mockk(relaxed = true)

        every { mockGroup.pairing } returns mockPairing
        database = Database(mockDriver)
        manager = BankCommitmentManager(mockContext, mockGroup, mockDriver)
    }

    @Test
    fun `store and retrieve commitment`() {
        // Mock elements
        val mockPublicKey = mockk<Element>()
        val mockCommitment = mockk<Element>()
        val publicKeyBytes = "publicKey".toByteArray()
        val commitmentBytes = "commitment".toByteArray()

        every { mockPublicKey.toBytes() } returns publicKeyBytes
        every { mockCommitment.toBytes() } returns commitmentBytes
        every { mockPairing.g1.newElementFromBytes(commitmentBytes) } returns mockCommitment

        // Test storage
        manager.storeCommitment(mockPublicKey, mockCommitment)

        // Test retrieval
        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertEquals(mockCommitment, result)
    }

    @Test
    fun `get non-existent commitment returns null`() {
        val mockPublicKey = mockk<Element>()
        every { mockPublicKey.toBytes() } returns "nonexistent".toByteArray()

        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertNull(result)
    }

    @Test
    fun `overwrite existing commitment`() {
        val mockPublicKey = mockk<Element>()
        val mockCommitment1 = mockk<Element>()
        val mockCommitment2 = mockk<Element>()
        val publicKeyBytes = "key".toByteArray()
        val commitmentBytes1 = "commit1".toByteArray()
        val commitmentBytes2 = "commit2".toByteArray()

        every { mockPublicKey.toBytes() } returns publicKeyBytes
        every { mockCommitment1.toBytes() } returns commitmentBytes1
        every { mockCommitment2.toBytes() } returns commitmentBytes2
        every { mockPairing.g1.newElementFromBytes(commitmentBytes1) } returns mockCommitment1
        every { mockPairing.g1.newElementFromBytes(commitmentBytes2) } returns mockCommitment2

        manager.storeCommitment(mockPublicKey, mockCommitment1)
        manager.storeCommitment(mockPublicKey, mockCommitment2)

        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertEquals(mockCommitment2, result)
    }

    @Test
    fun `clear all commitments`() {
        val mockPublicKey = mockk<Element>()
        val mockCommitment = mockk<Element>()
        every { mockPublicKey.toBytes() } returns "key".toByteArray()
        every { mockCommitment.toBytes() } returns "commit".toByteArray()
        every { mockPairing.g1.newElementFromBytes(any()) } returns mockCommitment

        manager.storeCommitment(mockPublicKey, mockCommitment)
        manager.clearAllCommitments()

        val result = manager.getCommitmentByPublicKey(mockPublicKey)
        assertNull(result)
    }
}

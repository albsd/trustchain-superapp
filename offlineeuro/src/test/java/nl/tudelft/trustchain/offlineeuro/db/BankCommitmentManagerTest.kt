package nl.tudelft.trustchain.offlineeuro.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PedersenCommitment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BankCommitmentManagerTest {
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val group = BilinearGroup()
    private val manager = BankCommitmentManager(null, group, driver)

    @Before
    fun setUp() {
        manager.clearAllCommitments()
    }

    @Test
    fun `store and retrieve commitment`() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val message = group.pairing.zr.newElementFromBytes("test.jwt.token".toByteArray())
        val nonce = group.getRandomZr()
        val commitment = PedersenCommitment.createCommitment(group, message, nonce)

        manager.storeCommitment(userPublicKey, commitment)
        val retrievedCommitment = manager.getCommitmentByPublicKey(userPublicKey)

        assertNotNull("Commitment should not be null", retrievedCommitment)
        assertEquals("Commitment should match stored value", commitment, retrievedCommitment)
    }

    @Test
    fun `get non-existent commitment returns null`() {
        val unknownPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val result = manager.getCommitmentByPublicKey(unknownPublicKey)
        assertNull("Commitment should be null for unknown public key", result)
    }

    @Test
    fun `overwrite existing commitment`() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable

        val message1 = group.pairing.zr.newElementFromBytes("first.jwt.token".toByteArray())
        val nonce1 = group.getRandomZr()
        val commitment1 = PedersenCommitment.createCommitment(group, message1, nonce1)

        val message2 = group.pairing.zr.newElementFromBytes("second.jwt.token".toByteArray())
        val nonce2 = group.getRandomZr()
        val commitment2 = PedersenCommitment.createCommitment(group, message2, nonce2)

        manager.storeCommitment(userPublicKey, commitment1)
        manager.storeCommitment(userPublicKey, commitment2)

        val retrievedCommitment = manager.getCommitmentByPublicKey(userPublicKey)
        assertEquals("Should return the most recent commitment", commitment2, retrievedCommitment)
    }

    @Test
    fun `clear all commitments`() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val message = group.pairing.zr.newElementFromBytes("test.jwt.token".toByteArray())
        val nonce = group.getRandomZr()
        val commitment = PedersenCommitment.createCommitment(group, message, nonce)

        manager.storeCommitment(userPublicKey, commitment)
        assertNotNull(manager.getCommitmentByPublicKey(userPublicKey))

        manager.clearAllCommitments()
        assertNull(manager.getCommitmentByPublicKey(userPublicKey))
    }

    @Test
    fun `commitment is properly reconstructed from bytes`() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val message = group.pairing.zr.newElementFromBytes("test.jwt.token".toByteArray())
        val nonce = group.getRandomZr()
        val expectedCommitment = PedersenCommitment.createCommitment(group, message, nonce)


        manager.storeCommitment(userPublicKey, expectedCommitment)
        val retrievedCommitment = manager.getCommitmentByPublicKey(userPublicKey)

        assertNotNull(retrievedCommitment)
        assertEquals(
            "Commitment should be properly reconstructed from bytes",
            expectedCommitment,
            retrievedCommitment
        )
    }
}

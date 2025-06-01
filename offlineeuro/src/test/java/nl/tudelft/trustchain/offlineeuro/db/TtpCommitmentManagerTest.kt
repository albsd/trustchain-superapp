package nl.tudelft.trustchain.offlineeuro.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TtpCommitmentManagerTest {
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val group = BilinearGroup()
    private val manager = TtpCommitmentManager(null, group, driver)

    @Before
    fun setUp() {
        manager.clearAllCommitments()
    }

    @Test
    fun storeAndRetrieveCommitment() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val jwtToken = "test.jwt.token"
        val nonce = group.getRandomZr()

        manager.storeCommitment(userPublicKey, jwtToken, nonce)
        val (retrievedJwt, retrievedNonce) = manager.getCommitmentByPublicKey(userPublicKey)!!

        assertEquals("JWT token should match", jwtToken, retrievedJwt)
        assertEquals("Nonce should match", nonce, retrievedNonce)
    }

    @Test
    fun getNonExistentCommitmentReturnsNull() {
        val unknownPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val result = manager.getCommitmentByPublicKey(unknownPublicKey)
        assertNull("Should return null for unknown public key", result)
    }

    @Test
    fun overwriteExistingCommitment() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable

        val jwtToken1 = "first.jwt.token"
        val nonce1 = group.getRandomZr()

        val jwtToken2 = "second.jwt.token"
        val nonce2 = group.getRandomZr()

        manager.storeCommitment(userPublicKey, jwtToken1, nonce1)
        manager.storeCommitment(userPublicKey, jwtToken2, nonce2)

        val (retrievedJwt, retrievedNonce) = manager.getCommitmentByPublicKey(userPublicKey)!!
        assertEquals("Should return the most recent JWT", jwtToken2, retrievedJwt)
        assertEquals("Should return the most recent nonce", nonce2, retrievedNonce)
    }

    @Test
    fun clearAllCommitments() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val jwtToken = "test.jwt.token"
        val nonce = group.getRandomZr()

        manager.storeCommitment(userPublicKey, jwtToken, nonce)
        assertNotNull(manager.getCommitmentByPublicKey(userPublicKey))

        manager.clearAllCommitments()
        assertNull(manager.getCommitmentByPublicKey(userPublicKey))
    }

    @Test
    fun nonceIsProperlyReconstructedFromBytes() {
        val userPublicKey = group.g.powZn(group.getRandomZr()).immutable
        val jwtToken = "test.jwt.token"
        val expectedNonce = group.getRandomZr()

        manager.storeCommitment(userPublicKey, jwtToken, expectedNonce)
        val (_, retrievedNonce) = manager.getCommitmentByPublicKey(userPublicKey)!!

        assertEquals(
            "Nonce should be properly reconstructed from bytes",
            expectedNonce,
            retrievedNonce
        )
    }
}

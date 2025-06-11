package nl.tudelft.trustchain.offlineeuro.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SignedPublicKeyManagerTest {
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val group = BilinearGroup()
    private val manager = SignedPublicKeyManager(null, driver)

    @Before
    fun setUp() {
        manager.clearSignedPublicKey()
    }

    @After
    fun tearDown() {
        manager.clearSignedPublicKey()
    }

    @Test
    fun storeAndGetSignedPublicKey() {
        // Create a test signature
        val privateKey = group.getRandomZr()
        val publicKey = group.g.powZn(privateKey).immutable
        val message = "test message".toByteArray()
        val signature = Schnorr.schnorrSignature(privateKey, message, group)

        // Store and retrieve
        manager.storeSignedPublicKey(signature)
        val retrieved = manager.getSignedPublicKey()

        // Verify
        assertEquals(signature, retrieved)
        assertEquals(true, Schnorr.verifySchnorrSignature(retrieved!!, publicKey, group))
    }

    @Test
    fun getSignedPublicKey_whenEmpty_returnsNull() {
        assertNull(manager.getSignedPublicKey())
    }

    @Test
    fun clearSignedPublicKey() {
        // Create and store a signature
        val privateKey = group.getRandomZr()
        val message = "test message".toByteArray()
        val signature = Schnorr.schnorrSignature(privateKey, message, group)
        manager.storeSignedPublicKey(signature)

        // Clear and verify
        manager.clearSignedPublicKey()
        assertNull(manager.getSignedPublicKey())
    }

    @Test
    fun storeSignedPublicKey_overwritesExisting() {
        // Create two different signatures
        val privateKey = group.getRandomZr()
        val message1 = "test message 1".toByteArray()
        val message2 = "test message 2".toByteArray()
        val signature1 = Schnorr.schnorrSignature(privateKey, message1, group)
        val signature2 = Schnorr.schnorrSignature(privateKey, message2, group)

        // Store first signature
        manager.storeSignedPublicKey(signature1)
        assertEquals(signature1, manager.getSignedPublicKey())

        // Store second signature and verify it overwrote the first
        manager.storeSignedPublicKey(signature2)
        assertEquals(signature2, manager.getSignedPublicKey())
    }
}

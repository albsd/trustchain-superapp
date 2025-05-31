package nl.tudelft.trustchain.offlineeuro.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RegisteredUserManagerTest {
    private val driver =
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
            Database.Schema.create(this)
        }

    private val group = BilinearGroup()
    private val registeredUserManager = RegisteredUserManager(null, group, driver)

    @Before
    fun before() {
        registeredUserManager.clearAllRegisteredUsers()
    }

    @Test
    fun addAndRetrieveTest() {
        val name = "Tester"
        val privateKey = group.getRandomZr()
        val publicKey = group.g.powZn(privateKey).immutable
        val signature = Schnorr.schnorrSignature(privateKey, publicKey.toBytes(), group)
        val transactionId = "transaction_id_123"
        val registrationResult = registeredUserManager.addRegisteredUser(name, publicKey, signature, transactionId)
        Assert.assertTrue("The registration should be successful", registrationResult)

        val findByName = registeredUserManager.getRegisteredUserByName(name)!!
        Assert.assertEquals("The name should match", name, findByName.name)
        Assert.assertEquals("The public key should match", publicKey, findByName.publicKey)
        Assert.assertEquals("The transactionId should match", transactionId, findByName.transactionId)
        Assert.assertEquals("The user should not be verified", 0, findByName.isVerified)

        val findByPublicKey = registeredUserManager.getRegisteredUserByPublicKey(publicKey)!!
        Assert.assertEquals("The name should match", name, findByPublicKey.name)

        val allUsers = registeredUserManager.getAllRegisteredUsers()
        Assert.assertEquals("No user should be verified yet", 0, allUsers.size)
    }

    @Test
    fun verifyUserByPublicKeyTest() {
        val name = "Verifier"
        val privateKey = group.getRandomZr()
        val publicKey = group.g.powZn(privateKey).immutable
        val transactionId = "txn_456"

        registeredUserManager.addRegisteredUser(name, publicKey, transactionId)

        // Initially should not be verified
        val userBeforeVerify = registeredUserManager.getRegisteredUserByName(name)!!
        Assert.assertEquals("Initially user should not be verified", 0, userBeforeVerify.isVerified)

        // Mark as verified
        val verifyResult = registeredUserManager.verifyUserByPublicKey(publicKey)
        Assert.assertTrue("Verification should succeed", verifyResult)

        // Now the user should be verified
        val userAfterVerify = registeredUserManager.getRegisteredUserByName(name)!!
        Assert.assertEquals("User should be verified", 1, userAfterVerify.isVerified)

        // Should now appear in getAllRegisteredUsers
        val verifiedUsers = registeredUserManager.getAllRegisteredUsers()
        Assert.assertEquals("Only one user should be verified", 1, verifiedUsers.size)
        Assert.assertEquals("Verified user's name should match", name, verifiedUsers[0].name)
    }

    @Test
    fun noUsersInitiallyTest() {
        val allUsers = registeredUserManager.getAllRegisteredUsers()
        Assert.assertTrue("No users should be returned", allUsers.isEmpty())
    }

    @Test
    fun unverifiedUserShouldNotAppearInVerifiedList() {
        val name = "InvisibleUser"
        val privateKey = group.getRandomZr()
        val publicKey = group.g.powZn(privateKey).immutable
        val transactionId = "txn_invisible"

        registeredUserManager.addRegisteredUser(name, publicKey, transactionId)

        val allVerified = registeredUserManager.getAllRegisteredUsers()
        Assert.assertTrue("Unverified users should not appear in verified list", allVerified.isEmpty())
    }
}

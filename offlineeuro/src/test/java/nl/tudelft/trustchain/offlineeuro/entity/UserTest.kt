package nl.tudelft.trustchain.offlineeuro.entity

import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.CRSGenerator
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.db.SignedPublicKeyManager
import nl.tudelft.trustchain.offlineeuro.db.WalletManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigInteger

class UserTest {
    private lateinit var group: BilinearGroup
    private lateinit var addressBookManager: AddressBookManager
    private lateinit var walletManager: WalletManager
    private lateinit var signedPublicKeyManager: SignedPublicKeyManager
    private lateinit var communicationProtocol: IPV8CommunicationProtocol
    private lateinit var user: User

    @Before
    fun setUp() {
        group = BilinearGroup(PairingTypes.A)
        addressBookManager = mock()
        walletManager = mock()
        signedPublicKeyManager = mock()
        communicationProtocol = mock()

        // Set up the mocks properly
        whenever(communicationProtocol.addressBookManager).thenReturn(addressBookManager)

        // Use doAnswer for the getGroupDescriptionAndCRS stub
        doAnswer {
            val generatedCRS = CRSGenerator.generateCRSMap(group)
            generatedCRS.first
        }.whenever(communicationProtocol).getGroupDescriptionAndCRS()

        user = User(
            "TestUser",
            group,
            null,
            walletManager,
            signedPublicKeyManager,
            communicationProtocol,
            runSetup = false,
        )
    }

    @Test
    fun storeSignedPublicKey_storesInMemoryAndDatabase() {
        val signature = SchnorrSignature(BigInteger.ONE, BigInteger.ONE, "test".toByteArray())
        user.storeSignedPublicKey(signature)

        verify(signedPublicKeyManager).storeSignedPublicKey(signature)
        assertEquals(signature, user.getSignedPublicKey())
    }

    @Test
    fun getSignedPublicKey_retrievesFromDatabaseIfNotInMemory() {
        val signature = SchnorrSignature(BigInteger.ONE, BigInteger.ONE, "test".toByteArray())
        whenever(signedPublicKeyManager.getSignedPublicKey()).thenReturn(signature)

        val retrieved = user.getSignedPublicKey()
        assertEquals(signature, retrieved)
    }

    @Test
    fun getSignedPublicKey_returnsNullIfNotInMemoryOrDatabase() {
        whenever(signedPublicKeyManager.getSignedPublicKey()).thenReturn(null)
        assertNull(user.getSignedPublicKey())
    }

    @Test
    fun reset_clearsSignedPublicKey() {
        val signature = SchnorrSignature(BigInteger.ONE, BigInteger.ONE, "test".toByteArray())
        user.storeSignedPublicKey(signature)
        assertEquals(signature, user.getSignedPublicKey())
        whenever(signedPublicKeyManager.getSignedPublicKey()).thenReturn(null)
        user.reset()
        verify(signedPublicKeyManager).clearSignedPublicKey()
        assertNull(user.getSignedPublicKey())
    }
}

package nl.tudelft.trustchain.offlineeuro.entity

import android.content.Context
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import nl.tudelft.trustchain.offlineeuro.db.RegisteredUserManager
import nl.tudelft.trustchain.offlineeuro.db.TtpCommitmentManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
class TTPTest {

    private lateinit var ttp: TTP
    private val group = BilinearGroup(PairingTypes.FromFile)
    private lateinit var mockCommProtocol: ICommunicationProtocol
    private lateinit var mockContext: Context
    private lateinit var mockRegUserManager: RegisteredUserManager
    private lateinit var mockCommitmentManager: TtpCommitmentManager
    private lateinit var userPublicKey: Element

    @Before
    fun setup() {
        mockCommProtocol = mock()
        mockContext = mock()
        mockRegUserManager = mock()
        mockCommitmentManager = mock()

        userPublicKey = group.generateRandomElementOfG()

        ttp = TTP(
            name = "TTP",
            group = group,
            communicationProtocol = mockCommProtocol,
            context = mockContext,
            registeredUserManager = mockRegUserManager,
            commitmentManager = mockCommitmentManager
        )
    }

    @Test
    fun signUserPK() {
        val signature = ttp.signUserPK(userPublicKey)

        assertNotNull(signature)

        val ttpPublicKey = ttp.publicKey
        val isValid = Schnorr.verifySchnorrSignature(signature, ttpPublicKey, group)
        assertTrue("Signature should be valid", isValid)
    }

    @Test
    fun getSignedUserPublicKey() {
        val signature = ttp.signUserPK(userPublicKey)

        val registeredUser = RegisteredUser(
            id = 1L,
            name = "TestUser",
            publicKey = userPublicKey,
            signedPublicKey = signature,
            transactionId = "tx123",
            isVerified = 1L
        )

        whenever(mockRegUserManager.getRegisteredUserByPublicKey(userPublicKey)).thenReturn(registeredUser)

        val fetchedSignature = ttp.getSignedUserPublicKey(userPublicKey)
        assertEquals(signature, fetchedSignature)
    }

    @Test
    fun generateAndStoreJwtCommitment() {
        val jwt = "mock-jwt"

        val commitment = ttp.generateAndStoreJwtCommitment(userPublicKey, jwt)

        val elementCaptor = argumentCaptor<Element>()
        val jwtCaptor = argumentCaptor<String>()
        val commitmentCaptor = argumentCaptor<Element>()

        org.mockito.kotlin.verify(mockCommitmentManager).storeCommitment(
            elementCaptor.capture(),
            jwtCaptor.capture(),
            commitmentCaptor.capture()
        )

        assertEquals(userPublicKey, elementCaptor.firstValue)
        assertEquals(jwt, jwtCaptor.firstValue)
        assertNotNull(commitmentCaptor.firstValue)
    }

    @Test
    fun signUserPKRandomness() {
        val sig1 = ttp.signUserPK(userPublicKey)
        val sig2 = ttp.signUserPK(userPublicKey)

        assertNotNull(sig1)
        assertNotNull(sig2)
        assertTrue(sig1 != sig2)
    }

    @Test
    fun isUserPublicKeyRegistered() {
        val signature = ttp.signUserPK(userPublicKey)

        val registeredUser = RegisteredUser(
            id = 1L,
            name = "TestUser",
            publicKey = userPublicKey,
            signedPublicKey = signature,
            transactionId = "tx123",
            isVerified = 1L
        )

        whenever(mockRegUserManager.getAllRegisteredUsers()).thenReturn(listOf(registeredUser))

        assertTrue(ttp.isUserPublicKeyRegistered(userPublicKey))
    }
}

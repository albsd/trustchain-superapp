package nl.tudelft.trustchain.offlineeuro.entity

import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import nl.tudelft.trustchain.offlineeuro.community.message.AddressMessage
import nl.tudelft.trustchain.offlineeuro.community.message.BilinearGroupCRSReplyMessage
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.CRSGenerator
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.db.DepositedEuroManager
import nl.tudelft.trustchain.offlineeuro.db.BankCommitmentManager
import nl.tudelft.trustchain.offlineeuro.enums.Role
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigInteger
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import android.util.Log
import org.junit.Before
import org.junit.After

class BankTest {
    private val ttpGroup = BilinearGroup(PairingTypes.FromFile)
    private val crs = CRSGenerator.generateCRSMap(ttpGroup).first
    private val depositedEuroManager = Mockito.mock(DepositedEuroManager::class.java)
    private val bankCommitmentManager = Mockito.mock(BankCommitmentManager::class.java)
    private lateinit var mockedLog: MockedStatic<Log>

    @Before
    fun setUp() {
        mockedLog = mockStatic(Log::class.java)
        mockedLog.`when`<Int> { Log.i(any(), any()) }.thenReturn(0)
    }

    @After
    fun tearDown() {
        mockedLog.close()
    }

    @Test
    fun initWithSetupTest() {
        val addressBookManager = Mockito.mock(AddressBookManager::class.java)
        val community = Mockito.mock(OfflineEuroCommunity::class.java)
        val communicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)

        whenever(community.messageList).thenReturn(communicationProtocol.messageList)
        whenever(community.getGroupDescriptionAndCRS()).then {
            communicationProtocol.messageList.add(
                BilinearGroupCRSReplyMessage(
                    ttpGroup.toGroupElementBytes(),
                    crs.toCRSBytes(),
                    AddressMessage("TTP", Role.TTP, "SomeBytes".toByteArray(), "More Bytes".toByteArray())
                )
            )
        }
        val ttpAddress = Address("TTP", Role.TTP, ttpGroup.generateRandomElementOfG(), "More Bytes".toByteArray())

        val publicKeyCaptor = argumentCaptor<ByteArray>()

        whenever(addressBookManager.getAddressByName("TTP")).thenReturn(ttpAddress)
        whenever(community.registerAtTTP(any(), publicKeyCaptor.capture(), any())).then { }

        val bankName = "SomeBank"
        val bank = Bank(bankName, BilinearGroup(PairingTypes.FromFile), communicationProtocol, null, depositedEuroManager, bankCommitmentManager)

        val capturedPKBytes = publicKeyCaptor.firstValue
        val capturedPK = ttpGroup.gElementFromBytes(capturedPKBytes)

        Assert.assertEquals(bankName, bank.name)
        Assert.assertEquals(ttpGroup, bank.group)
        Assert.assertEquals(crs, bank.crs)
        Assert.assertEquals(bank.publicKey, capturedPK)
        Assert.assertEquals(bank, communicationProtocol.participant)
    }

    @Test
    fun initWithoutSetUpTest() {
        val addressBookManager = Mockito.mock(AddressBookManager::class.java)
        val community = Mockito.mock(OfflineEuroCommunity::class.java)
        val communicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)

        val bankName = "SomeOtherBank"
        val group = BilinearGroup(PairingTypes.FromFile)
        val bank = Bank(bankName, group, communicationProtocol, null, depositedEuroManager, bankCommitmentManager, false)

        verify(community, never()).getGroupDescriptionAndCRS()
        verify(community, never()).registerAtTTP(any(), any(), any())
        Assert.assertEquals(group, bank.group)

        Assert.assertThrows(UninitializedPropertyAccessException::class.java) {
            bank.crs.toCRSBytes()
        }
    }

    @Test
    fun getBlindSignatureRandomnessTest() {
        val bank = getBank()
        val publicKey = ttpGroup.generateRandomElementOfG()

        val firstRandomness = bank.getBlindSignatureRandomness(publicKey)
        val secondRandomness = bank.getBlindSignatureRandomness(publicKey)

        Assert.assertEquals("The same randomness should be returned", firstRandomness, secondRandomness)

        val newPublicKey = ttpGroup.generateRandomElementOfG()
        val thirdRandomness = bank.getBlindSignatureRandomness(newPublicKey)

        Assert.assertNotEquals(firstRandomness, thirdRandomness)
    }

    @Test
    fun getBlindSignatureTest() {
        val bank = getBank()
        val publicKey = ttpGroup.generateRandomElementOfG()
        val firstRandomness = bank.getBlindSignatureRandomness(publicKey)
        val elementToSign = ttpGroup.generateRandomElementOfG().immutable.toBytes()
        val serialNumber = "TestSerialNumber"
        val bytesToSign = serialNumber.toByteArray() + elementToSign

        val blindedChallenge = Schnorr.createBlindedChallenge(firstRandomness, bytesToSign, bank.publicKey, ttpGroup)
        val blindSignature = bank.createBlindSignature(blindedChallenge.blindedChallenge, publicKey)
        val blindSchnorrSignature = Schnorr.unblindSignature(blindedChallenge, blindSignature)
        Assert.assertTrue(Schnorr.verifySchnorrSignature(blindSchnorrSignature, bank.publicKey, ttpGroup))

        val noRandomnessRequestedKey = ttpGroup.generateRandomElementOfG()
        val response = bank.createBlindSignature(blindedChallenge.blindedChallenge, noRandomnessRequestedKey)
        Assert.assertEquals("There should be no randomness found", BigInteger.ZERO, response)
    }

    @Test
    fun storeUserCommitment() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val mockCommitment = ttpGroup.generateRandomElementOfG()

        bank.storeUserCommitment(userPublicKey, mockCommitment)

        verify(bankCommitmentManager).storeCommitment(userPublicKey, mockCommitment)
    }

    @Test
    fun verifyRevealedCommitmentValid() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val jwtToken = "valid.jwt.token"
        val nonce = ttpGroup.getRandomZr()
        val message = ttpGroup.pairing.zr.newElementFromBytes(jwtToken.toByteArray())
        val expectedCommitment = ttpGroup.g.powZn(message).mul(ttpGroup.h.powZn(nonce))

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(expectedCommitment)

        val isValid = bank.verifyRevealedCommitment(userPublicKey, jwtToken, nonce)
        Assert.assertTrue(isValid)
    }

    @Test
    fun verifyRevealedCommitmentInvalidJwt() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val nonce = ttpGroup.getRandomZr()
        val validCommitment = ttpGroup.generateRandomElementOfG()

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(validCommitment)

        val isValid = bank.verifyRevealedCommitment(userPublicKey, "invalid.jwt.token", nonce)
        Assert.assertFalse(isValid)
    }

    @Test
    fun verifyRevealedCommitmentInvalidNonce() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val jwtToken = "valid.jwt.token"
        val wrongNonce = ttpGroup.getRandomZr()
        val validCommitment = ttpGroup.generateRandomElementOfG()

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(validCommitment)

        val isValid = bank.verifyRevealedCommitment(userPublicKey, jwtToken, wrongNonce)
        Assert.assertFalse(isValid)
    }

    @Test
    fun verifyRevealedCommitmentNoStoredCommitment() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(null)

        val isValid = bank.verifyRevealedCommitment(
            userPublicKey,
            "any.jwt.token",
            ttpGroup.getRandomZr()
        )
        Assert.assertFalse(isValid)
    }

    @Test
    fun completeCommitmentFlow() {
        // Setup
        val bank = getBank()
        val mockTtp = Mockito.mock(TTP::class.java)
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val jwtToken = "user.jwt.token"
        val nonce = ttpGroup.getRandomZr()
        val message = ttpGroup.pairing.zr.newElementFromBytes(jwtToken.toByteArray())
        val commitment = ttpGroup.g.powZn(message).mul(ttpGroup.h.powZn(nonce))

        // TTP generates and stores commitment
        whenever(mockTtp.generateAndStoreJwtCommitment(userPublicKey, jwtToken))
            .thenReturn(commitment)

        // Bank stores the commitment
        bank.storeUserCommitment(userPublicKey, commitment)

        // Later verification
        whenever(mockTtp.revealCommitment(userPublicKey))
            .thenReturn(jwtToken to nonce)

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(commitment)

        val isValid = bank.verifyRevealedCommitment(userPublicKey, jwtToken, nonce)
        Assert.assertTrue(isValid)
    }

    @Test
    fun verifyWithEmptyJwt() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val nonce = ttpGroup.getRandomZr()
        val emptyJwt = ""

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(ttpGroup.generateRandomElementOfG())

        val isValid = bank.verifyRevealedCommitment(userPublicKey, emptyJwt, nonce)
        Assert.assertFalse(isValid)
    }

    @Test
    fun verifyWithZeroNonce() {
        val bank = getBank()
        val userPublicKey = ttpGroup.generateRandomElementOfG()
        val jwtToken = "user.jwt.token"
        val zeroNonce = ttpGroup.pairing.zr.newZeroElement()

        whenever(bankCommitmentManager.getCommitmentByPublicKey(userPublicKey))
            .thenReturn(ttpGroup.generateRandomElementOfG())

        val isValid = bank.verifyRevealedCommitment(userPublicKey, jwtToken, zeroNonce)
        Assert.assertFalse(isValid)
    }

    fun getBank(): Bank {
        val addressBookManager = Mockito.mock(AddressBookManager::class.java)
        val community = Mockito.mock(OfflineEuroCommunity::class.java)
        val communicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)

        val bankName = "Bank"
        val group = ttpGroup
        val bank = Bank(bankName, group, communicationProtocol, null, depositedEuroManager, bankCommitmentManager, false)
        bank.crs = crs
        return bank
    }
}

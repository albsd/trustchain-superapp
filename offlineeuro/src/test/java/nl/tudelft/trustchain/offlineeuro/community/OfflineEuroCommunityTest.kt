package nl.tudelft.trustchain.offlineeuro.community

import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.attestation.trustchain.TrustChainSettings
import nl.tudelft.ipv8.attestation.trustchain.store.TrustChainStore
import nl.tudelft.trustchain.offlineeuro.community.message.*
import nl.tudelft.trustchain.offlineeuro.community.payload.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import nl.tudelft.trustchain.offlineeuro.cryptography.RandomizationElementsBytes
import java.math.BigInteger

class OfflineEuroCommunityTest {
    @Mock
    private lateinit var settings: TrustChainSettings

    @Mock
    private lateinit var database: TrustChainStore

    @Mock
    private lateinit var peer: Peer

    private lateinit var community: OfflineEuroCommunity

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        community = OfflineEuroCommunity(settings, database)
    }

    @Test
    fun testTTPCommitmentMessage() {
        val commitmentBytes = "test_commitment".toByteArray()
        val message = TTPCommitmentMessage(commitmentBytes)

        Assert.assertEquals(CommunityMessageType.TTPCommitmentMessage, message.messageType)
        Assert.assertArrayEquals(commitmentBytes, message.commitmentBytes)
    }

    @Test
    fun testTTPVerificationCompleteMessage() {
        val userName = "test_user"
        val userPKBytes = "test_public_key".toByteArray()
        val message = TTPVerificationCompleteMessage(userName, userPKBytes, peer)

        Assert.assertEquals(CommunityMessageType.TTPVerificationCompleteMessage, message.messageType)
        Assert.assertEquals(userName, message.userName)
        Assert.assertArrayEquals(userPKBytes, message.userPKBytes)
        Assert.assertEquals(peer, message.peer)
    }

    @Test
    fun testTTPVerificationRequestPayload() {
        val clientId = "test_client"
        val requestUri = "test_uri"
        val requestUriMethod = "GET"

        val payload = TTPVerificationRequestPayload(clientId, requestUri, requestUriMethod)
        val serialized = payload.serialize()
        val (deserialized, _) = TTPVerificationRequestPayload.deserialize(serialized, 0)

        Assert.assertEquals(clientId, deserialized.clientId)
        Assert.assertEquals(requestUri, deserialized.requestUri)
        Assert.assertEquals(requestUriMethod, deserialized.requestUriMethod)
    }

    @Test
    fun testTTPVerificationCompletePayload() {
        val userName = "test_user"
        val publicKey = "test_public_key".toByteArray()

        val payload = TTPVerificationCompletePayload(userName, publicKey)
        val serialized = payload.serialize()
        val (deserialized, _) = TTPVerificationCompletePayload.deserialize(serialized, 0)

        Assert.assertEquals(userName, deserialized.userName)
        Assert.assertArrayEquals(publicKey, deserialized.publicKey)
    }

    @Test
    fun testTransactionRandomizationElementsPayload() {
        val group2T = "test_group2T".toByteArray()
        val vT = "test_vT".toByteArray()
        val group1TInv = "test_group1TInv".toByteArray()
        val uTInv = "test_uTInv".toByteArray()

        val randomizationElements = RandomizationElementsBytes(group2T, vT, group1TInv, uTInv)
        val payload = TransactionRandomizationElementsPayload(randomizationElements)
        val serialized = payload.serialize()
        val (deserialized, _) = TransactionRandomizationElementsPayload.deserialize(serialized, 0)

        Assert.assertArrayEquals(group2T, deserialized.transactionRandomizationElementsBytes.group2T)
        Assert.assertArrayEquals(vT, deserialized.transactionRandomizationElementsBytes.vT)
        Assert.assertArrayEquals(group1TInv, deserialized.transactionRandomizationElementsBytes.group1TInv)
        Assert.assertArrayEquals(uTInv, deserialized.transactionRandomizationElementsBytes.uTInv)
    }

    @Test
    fun testBlindSignatureRequestMessage() {
        val challenge = BigInteger("123456789")
        val publicKeyBytes = "test_public_key".toByteArray()
        val message = BlindSignatureRequestMessage(challenge, publicKeyBytes, peer)

        Assert.assertEquals(CommunityMessageType.BlindSignatureRequestMessage, message.messageType)
        Assert.assertEquals(challenge, message.challenge)
        Assert.assertArrayEquals(publicKeyBytes, message.publicKeyBytes)
        Assert.assertEquals(peer, message.peer)
    }

    @Test
    fun testFraudControlRequestMessage() {
        val firstProofBytes = "test_first_proof".toByteArray()
        val secondProofBytes = "test_second_proof".toByteArray()
        val serialNumber = "serial_number"

        val message = FraudControlRequestMessage(serialNumber, firstProofBytes, secondProofBytes, peer)

        Assert.assertEquals(CommunityMessageType.FraudControlRequestMessage, message.messageType)
        Assert.assertArrayEquals(firstProofBytes, message.firstProofBytes)
        Assert.assertArrayEquals(secondProofBytes, message.secondProofBytes)
        Assert.assertEquals(peer, message.requestingPeer)
    }
}

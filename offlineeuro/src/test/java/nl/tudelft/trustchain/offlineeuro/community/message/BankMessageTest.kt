package nl.tudelft.trustchain.offlineeuro.community.message

import nl.tudelft.ipv8.Peer
import nl.tudelft.trustchain.offlineeuro.community.payload.*
import nl.tudelft.trustchain.offlineeuro.cryptography.RandomizationElementsBytes
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.math.BigInteger

class BankMessageTest {
    @Mock
    private lateinit var peer: Peer

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testBlindSignatureRandomnessRequestMessage() {
        val publicKeyBytes = "test_public_key".toByteArray()
        val message = BlindSignatureRandomnessRequestMessage(publicKeyBytes, peer)

        Assert.assertEquals(CommunityMessageType.BlindSignatureRandomnessRequestMessage, message.messageType)
        Assert.assertArrayEquals(publicKeyBytes, message.publicKeyBytes)
        Assert.assertEquals(peer, message.peer)
    }

    @Test
    fun testBlindSignatureRandomnessReplyMessage() {
        val randomnessBytes = "test_randomness".toByteArray()
        val message = BlindSignatureRandomnessReplyMessage(randomnessBytes)

        Assert.assertEquals(CommunityMessageType.BlindSignatureRandomnessReplyMessage, message.messageType)
        Assert.assertArrayEquals(randomnessBytes, message.randomnessBytes)
    }

    @Test
    fun testBlindSignatureReplyMessage() {
        val signature = BigInteger("123456789")
        val message = BlindSignatureReplyMessage(signature)

        Assert.assertEquals(CommunityMessageType.BlindSignatureReplyMessage, message.messageType)
        Assert.assertEquals(signature, message.signature)
    }

    @Test
    fun testTransactionRandomizationElementsRequestMessage() {
        val publicKey = "test_public_key".toByteArray()
        val message = TransactionRandomizationElementsRequestMessage(publicKey, peer)

        Assert.assertEquals(CommunityMessageType.TransactionRandomnessRequestMessage, message.messageType)
        Assert.assertArrayEquals(publicKey, message.publicKey)
        Assert.assertEquals(peer, message.requestingPeer)
    }

    @Test
    fun testTransactionRandomizationElementsReplyMessage() {
        val group2T = "test_group2T".toByteArray()
        val vT = "test_vT".toByteArray()
        val group1TInv = "test_group1TInv".toByteArray()
        val uTInv = "test_uTInv".toByteArray()

        val randomizationElements = RandomizationElementsBytes(group2T, vT, group1TInv, uTInv)
        val message = TransactionRandomizationElementsReplyMessage(randomizationElements)

        Assert.assertEquals(CommunityMessageType.TransactionRandomnessReplyMessage, message.messageType)
        Assert.assertEquals(randomizationElements, message.randomizationElementsBytes)
    }

    @Test
    fun testTransactionResultMessage() {
        val result = "Transaction successful"
        val message = TransactionResultMessage(result)

        Assert.assertEquals(CommunityMessageType.TransactionResultMessage, message.messageType)
        Assert.assertEquals(result, message.result)
    }

    @Test
    fun testBlindSignatureRequestPayload() {
        val challenge = BigInteger("123456789")
        val publicKeyBytes = "test_public_key".toByteArray()

        val payload = BlindSignatureRequestPayload(challenge, publicKeyBytes)
        val serialized = payload.serialize()
        val (deserialized, _) = BlindSignatureRequestPayload.deserialize(serialized, 0)

        Assert.assertEquals(challenge, deserialized.challenge)
        Assert.assertArrayEquals(publicKeyBytes, deserialized.publicKeyBytes)
    }

    @Test
    fun testFraudControlRequestPayload() {
        val firstProofBytes = "test_first_proof".toByteArray()
        val secondProofBytes = "test_second_proof".toByteArray()
        val serialNumber = "serial_number"

        val payload = FraudControlRequestPayload(serialNumber, firstProofBytes, secondProofBytes)
        val serialized = payload.serialize()
        val (deserialized, _) = FraudControlRequestPayload.deserialize(serialized, 0)

        Assert.assertArrayEquals(firstProofBytes, deserialized.firstProofBytes)
        Assert.assertArrayEquals(secondProofBytes, deserialized.secondProofBytes)
    }
}

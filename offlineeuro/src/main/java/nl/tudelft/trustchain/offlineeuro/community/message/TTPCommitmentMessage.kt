package nl.tudelft.trustchain.offlineeuro.community.message

import nl.tudelft.ipv8.Peer
import nl.tudelft.trustchain.offlineeuro.community.payload.ByteArrayPayload

class TTPCommitmentMessage(
    val commitmentBytes: ByteArray,
) : ICommunityMessage {
    override val messageType = CommunityMessageType.TTPCommitmentMessage
}

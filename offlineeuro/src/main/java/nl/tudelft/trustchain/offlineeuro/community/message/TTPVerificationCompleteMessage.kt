package nl.tudelft.trustchain.offlineeuro.community.message

import nl.tudelft.ipv8.Peer

class TTPVerificationCompleteMessage (
    val userName: String,
    val userPKBytes: ByteArray,
    val peer: Peer
): ICommunityMessage {
    override val messageType = CommunityMessageType.TTPVerificationCompleteMessage
}

package nl.tudelft.trustchain.offlineeuro.community.message

import nl.tudelft.ipv8.Peer

class TTPRegistrationMessage(
    val userName: String,
    val userPKBytes: ByteArray,
    val peerPublicKeyBytes: ByteArray,
    val peer: Peer
) : ICommunityMessage {
    override val messageType = CommunityMessageType.TTPRegistrationMessage
}

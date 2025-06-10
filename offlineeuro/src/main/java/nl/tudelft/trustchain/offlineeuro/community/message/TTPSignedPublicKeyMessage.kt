package nl.tudelft.trustchain.offlineeuro.community.message

import nl.tudelft.ipv8.Peer
import nl.tudelft.trustchain.offlineeuro.community.payload.ByteArrayPayload

class TTPSignedPublicKeyMessage(
    val signedPublicKeyBytes: ByteArray,
    val peer: Peer
) : ICommunityMessage {
    override val messageType = CommunityMessageType.TTPSignedPublicKeyMessage
}

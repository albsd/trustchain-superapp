package nl.tudelft.trustchain.offlineeuro.community.message

class TTPRegistrationCompleteMessage(
    val status: String,
    val signedPK: ByteArray
) : ICommunityMessage {
    override val messageType = CommunityMessageType.TTPRegistrationCompleteMessage
}

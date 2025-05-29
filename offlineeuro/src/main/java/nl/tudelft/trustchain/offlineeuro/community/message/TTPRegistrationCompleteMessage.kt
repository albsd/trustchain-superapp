package nl.tudelft.trustchain.offlineeuro.community.message

class TTPRegistrationCompleteMessage(
    val status: String,
) : ICommunityMessage {
    override val messageType = CommunityMessageType.TTPRegistrationCompleteMessage
}

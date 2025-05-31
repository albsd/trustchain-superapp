package nl.tudelft.trustchain.offlineeuro.community.message

class TTPVerificationRequestMessage (
    val clientId: String,
    val requestUri: String,
    val requestUriMethod: String,
): ICommunityMessage {
    override val messageType = CommunityMessageType.TTPVerificationRequestMessage
}

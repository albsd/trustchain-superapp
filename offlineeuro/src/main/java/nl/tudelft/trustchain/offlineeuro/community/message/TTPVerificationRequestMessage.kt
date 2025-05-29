package nl.tudelft.trustchain.offlineeuro.community.message

class TTPVerificationRequestMessage (
    val verifierLink: String,
): ICommunityMessage {
    override val messageType = CommunityMessageType.TTPVerificationRequestMessage
}

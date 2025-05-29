package nl.tudelft.trustchain.offlineeuro.community.message

class TTPVerificationCompleteMessage (
    val status: String,
    val userName: String,
    val userPKBytes: ByteArray,
): ICommunityMessage {
    override val messageType = CommunityMessageType.TTPVerificationCompleteMessage
}

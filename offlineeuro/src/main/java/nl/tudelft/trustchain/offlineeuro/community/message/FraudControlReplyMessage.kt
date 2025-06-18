package nl.tudelft.trustchain.offlineeuro.community.message

import nl.tudelft.trustchain.offlineeuro.entity.FraudControlResult

class FraudControlReplyMessage(
    val serialNumber: String,
    val isFraud : Boolean,
    val jwtPlaintext: String?,
    val noncePlaintext : ByteArray?,
    val userName: String?,
    val pkBytes: ByteArray?
) : ICommunityMessage {
    override val messageType = CommunityMessageType.FraudControlReplyMessage
}

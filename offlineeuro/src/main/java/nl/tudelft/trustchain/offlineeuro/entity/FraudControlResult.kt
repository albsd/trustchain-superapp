package nl.tudelft.trustchain.offlineeuro.entity

import it.unisa.dia.gas.jpbc.Element

class FraudControlResult(
    val isFraud: Boolean,
    val jwt: String?,
    val nonce: Element?,
    val userName: String?,
    val userPK: Element?
) {

    fun isFraud() : Boolean {
        return isFraud;
    }

    override fun toString() : String {
        if(!isFraud)
            return "No double spending detected"
        else {
            if (userName == null || userPK == null)
                throw Exception("User cannot be null when fraud was detected")
            return "Double spending detected. Double spender is ${userName} with PK: ${userPK.toString()}"
        }
    }
}

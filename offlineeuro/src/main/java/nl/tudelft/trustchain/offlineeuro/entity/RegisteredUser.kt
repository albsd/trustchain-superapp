package nl.tudelft.trustchain.offlineeuro.entity

import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature

data class RegisteredUser(
    val id: Long,
    val name: String,
    val publicKey: Element,
    val signedPublicKey: SchnorrSignature,
    val transactionId: String,
    val isVerified: Long
)

package nl.tudelft.trustchain.offlineeuro.entity

import android.content.Context
import android.net.Uri
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.GrothSahaiProof


class DualRole(
    context: Context,
    group: BilinearGroup,
    communicationProtocol: ICommunicationProtocol,
    userName: String = "User",
) : Participant(communicationProtocol, userName) {

    val userName = userName

    private val user: User = User(
        name = userName,
        group = group,
        context = context,
        communicationProtocol = communicationProtocol,
        runSetup = true
    )

    private val ttp: TTP = TTP(
        name = userName,
        group = group,
        communicationProtocol = communicationProtocol,
        context = context
    )

    fun getTTP(): TTP {
        return ttp
    }

    fun getUser(): User {
        return user
    }


    // user
    fun authWith(deepLink: Uri) {
        user.authWith(deepLink)
    }

    fun sendDigitalEuroTo(nameReceiver: String): String {
        return user.sendDigitalEuroTo(nameReceiver)
    }

    fun getBalance(): Int {
        return user.getBalance()
    }

    fun getTokens(): List<WalletEntry> {
        return user.getTokens()
    }

    fun withdrawDigitalEuro(bank: String): DigitalEuro {
        return user.withdrawDigitalEuro(bank)
    }

    fun authStatus(status: String) {
        return user.authStatus(status)
    }


    // ttp
    fun registerUser(name: String, publicKey: Element): Map<String, String>? {
        return ttp.registerUser(name, publicKey)
    }

    fun verifyUser(name: String, publicKey: Element): Boolean {
        return ttp.verifyUser(name, publicKey)
    }

    fun getSignedUserPublicKey(publicKey: Element) = ttp.getSignedUserPublicKey(publicKey)

    fun isUserPublicKeyRegistered(publicKey: Element) = ttp.isUserPublicKeyRegistered(publicKey)

    fun generateAndStoreJwtCommitment(userPublicKey: Element, jwtToken: String): Element {
        return ttp.generateAndStoreJwtCommitment(userPublicKey, jwtToken)
    }

    fun revealCommitment(userPublicKey: Element): Pair<String, Element>? {
        return ttp.revealCommitment(userPublicKey)
    }

    fun getUserFromProof(grothSahaiProof: GrothSahaiProof): RegisteredUser? {
        return ttp.getUserFromProof(grothSahaiProof)
    }

    fun getUserFromProofs(firstProof: GrothSahaiProof, secondProof: GrothSahaiProof): String? {
        return ttp.getUserFromProofs(firstProof, secondProof)
    }

    override fun onReceivedTransaction(
        transactionDetails: TransactionDetails,
        publicKeyBank: Element,
        publicKeySender: Element
    ): String {
        return user.onReceivedTransaction(transactionDetails, publicKeyBank, publicKeySender)
    }

    override fun reset() {
        user.reset()
        ttp.reset()
    }
}

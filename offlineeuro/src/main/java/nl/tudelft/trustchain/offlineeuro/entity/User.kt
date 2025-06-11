package nl.tudelft.trustchain.offlineeuro.entity

import android.content.Context
import android.net.Uri
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import nl.tudelft.trustchain.offlineeuro.db.WalletManager
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.db.SignedPublicKeyManager
import nl.tudelft.trustchain.offlineeuro.enums.Role
import java.util.UUID

class User(
    name: String,
    group: BilinearGroup,
    context: Context?,
    private var walletManager: WalletManager? = null,
    private val signedPublicKeyManager: SignedPublicKeyManager? = SignedPublicKeyManager(context),
    communicationProtocol: ICommunicationProtocol,
    runSetup: Boolean = true
) : Participant(communicationProtocol, name) {
    val wallet: Wallet
    private val addressBookManager: AddressBookManager
    var authManager: EUDIAuthManager? = null
    private var _signedPublicKey: SchnorrSignature? = null
        get() = field ?: signedPublicKeyManager?.getSignedPublicKey()

    fun getSignedPublicKey(): SchnorrSignature? = _signedPublicKey

    init {
        communicationProtocol.participant = this
        this.group = group

        if (runSetup) {
            setUp()
        } else {
            generateKeyPair()
        }
        if (walletManager == null) {
            walletManager = WalletManager(context, group)
        }
        addressBookManager = (communicationProtocol as IPV8CommunicationProtocol).addressBookManager

        wallet = Wallet(privateKey, publicKey, walletManager!!)
    }

    fun sendDigitalEuroTo(nameReceiver: String, tokenSerialNumber: String): String {
        val randomizationElements = communicationProtocol.requestTransactionRandomness(nameReceiver, group)
        val transactionDetails =
            wallet.spendEuro(randomizationElements, group, crs, tokenSerialNumber)
                ?: throw Exception("No euro to spend")

        val result = communicationProtocol.sendTransactionDetails(nameReceiver, transactionDetails)
        emitEvent(result)
        return result
    }

    fun doubleSpendDigitalEuroTo(nameReceiver: String, tokenSerialNumber: String): String {
        val randomizationElements = communicationProtocol.requestTransactionRandomness(nameReceiver, group)
        val transactionDetails = wallet.doubleSpendEuro(randomizationElements, group, crs, tokenSerialNumber)
        val result = communicationProtocol.sendTransactionDetails(nameReceiver, transactionDetails!!)
        emitEvent(result)
        return result
    }

    fun withdrawDigitalEuro(bank: String): DigitalEuro {
        val serialNumber = UUID.randomUUID().toString()
        val firstT = group.getRandomZr()
        val tInv = firstT.mul(-1)
        val initialTheta = group.g.powZn(tInv).immutable

        val bytesToSign = serialNumber.toByteArray() + initialTheta.toBytes()

        val bankRandomness = communicationProtocol.getBlindSignatureRandomness(publicKey, bank, group)
        val bankPublicKey = communicationProtocol.getPublicKeyOf(bank, group)

        val blindedChallenge = Schnorr.createBlindedChallenge(bankRandomness, bytesToSign, bankPublicKey, group)
        val blindSignature = communicationProtocol.requestBlindSignature(publicKey, bank, blindedChallenge.blindedChallenge)
        val signature = Schnorr.unblindSignature(blindedChallenge, blindSignature)
        val digitalEuro = DigitalEuro(serialNumber, initialTheta, signature, arrayListOf())
        wallet.addToWallet(digitalEuro, firstT)
        emitEvent("Withdrawn ${digitalEuro.serialNumber} successfully!")
        return digitalEuro
    }

    fun retrieveScopedUsers(): List<String> {
        val addresses = addressBookManager.getAllAddresses()
        val usernames = addresses.filter { it.type == Role.User && it.name != name}.map { it.name }
        return usernames
    }

    fun getBalance(): Int {
        return walletManager!!.getWalletEntriesToSpend().count()
    }

    fun getTokens(): List<WalletEntry> {
        return walletManager!!.getWalletEntriesToSpend()
    }

    // only for demo purposes
    fun generateNewEuroDemo() {
        val walletEntry = wallet.generateWalletEntry()
        walletManager!!.insertWalletEntry(walletEntry)
    }

    fun authWith(uri: Uri) {
        authManager?.authWith(uri)
    }

    fun authStatus(status: String) {
        authManager?.authStatusUpdate(status)
    }

    override fun onReceivedTransaction(
        transactionDetails: TransactionDetails,
        publicKeyBank: Element,
        publicKeySender: Element
    ): String {
        val usedRandomness = lookUpRandomness(publicKeySender) ?: return "Randomness Not found!"
        removeRandomness(publicKeySender)
        val ttpPublicKey = communicationProtocol.getPublicKeyOf("TTP", group)
        val transactionResult = Transaction.validate(transactionDetails, publicKeyBank, group, crs, ttpPublicKey)

        if (transactionResult.valid) {
            wallet.addToWallet(transactionDetails, usedRandomness)
            emitEvent("Received an euro from $publicKeySender")
            return transactionResult.description
        }
        emitEvent(transactionResult.description)
        return transactionResult.description
    }

    override fun reset() {
        randomizationElementMap.clear()
        walletManager!!.clearWalletEntries()
        signedPublicKeyManager?.clearSignedPublicKey()
        _signedPublicKey = null
        setUp()
    }

    fun storeSignedPublicKey(signature: SchnorrSignature) {
        _signedPublicKey = signature
        signedPublicKeyManager?.storeSignedPublicKey(signature)
        wallet.updateUserSignedPublicKey(signature)
    }
}

package nl.tudelft.trustchain.offlineeuro.entity

import android.content.Context
import android.util.Log
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PedersenCommitment
import nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr
import nl.tudelft.trustchain.offlineeuro.db.BankCommitmentManager
import nl.tudelft.trustchain.offlineeuro.db.DepositedEuroManager
import java.math.BigInteger
import kotlin.math.min

class Bank(
    name: String,
    group: BilinearGroup,
    communicationProtocol: ICommunicationProtocol,
    context: Context?,
    private val depositedEuroManager: DepositedEuroManager = DepositedEuroManager(context, group),
    private val commitmentManager: BankCommitmentManager = BankCommitmentManager(context, group),
    runSetup: Boolean = true,
) : Participant(communicationProtocol, name) {
    private val depositedEuros: ArrayList<DigitalEuro> = arrayListOf()
    val withdrawUserRandomness: HashMap<Element, Element> = hashMapOf()
    val depositedEuroLogger: ArrayList<Pair<String, FraudControlResult>> = arrayListOf()
    private val userCommitments: MutableMap<Element, PedersenCommitment> = mutableMapOf()

    init {
        communicationProtocol.participant = this
        this.group = group
        if (runSetup) {
            setUp()
        } else {
            generateKeyPair()
        }
    }

    fun getBlindSignatureRandomness(userPublicKey: Element): Element {
        if (withdrawUserRandomness.containsKey(userPublicKey)) {
            val randomness = withdrawUserRandomness[userPublicKey]!!
            return group.g.powZn(randomness)
        }
        val randomness = group.getRandomZr()
        withdrawUserRandomness[userPublicKey] = randomness
        return group.g.powZn(randomness)
    }

    fun createBlindSignature(
        challenge: BigInteger,
        userPublicKey: Element
    ): BigInteger {
        val k =
            lookUp(userPublicKey)
                ?: return BigInteger.ZERO
        remove(userPublicKey)

        emitEvent("A token was withdrawn by $userPublicKey")
        // <Subtract balance here>
        return Schnorr.signBlindedChallenge(k, challenge, privateKey)
    }

    /**
     * Stores a user's commitment in the database
     */
    fun storeUserCommitment(userPublicKey: Element, commitment: Element) {
        commitmentManager.storeCommitment(userPublicKey, commitment)
    }

    /**
     * Verifies a revealed commitment against the stored commitment
     */
    fun verifyRevealedCommitment(
        userPublicKey: Element,
        plaintextJwt: String,
        nonce: Element
    ): Boolean {
        val storedCommitment = commitmentManager.getCommitmentByPublicKey(userPublicKey) ?: return false
        val message = group.pairing.zr.newElementFromBytes(plaintextJwt.toByteArray())
        return PedersenCommitment.verifyCommitment(group, storedCommitment, message, nonce)
    }

    private fun lookUp(userPublicKey: Element): Element? {
        for (element in withdrawUserRandomness.entries) {
            val key = element.key

            if (key == userPublicKey) {
                return element.value
            }
        }

        return null
    }

    private fun remove(userPublicKey: Element): Element? {
        for (element in withdrawUserRandomness.entries) {
            val key = element.key

            if (key == userPublicKey) {
                return withdrawUserRandomness.remove(key)
            }
        }

        return null
    }

    private fun depositEuro(
        euro: DigitalEuro,
        publicKeyUser: Element
    ): String {
        val duplicateEuros = depositedEuroManager.getDigitalEurosByDescriptor(euro)

        if (duplicateEuros.isEmpty()) {
            depositedEuroLogger.add(Pair(euro.serialNumber, FraudControlResult(false, null, null, null, null)))
            depositedEuroManager.insertDigitalEuro(euro)
            emitEvent("An euro was deposited successfully by $publicKeyUser")
            return "Deposit was successful!"
        }

        var maxFirstDifferenceIndex = -1
        var doubleSpendEuro: DigitalEuro? = null
        for (duplicateEuro in duplicateEuros) {
            // Loop over the proofs to find the double spending
            val euroProofs = euro.proofs
            val duplicateEuroProofs = duplicateEuro.proofs

            for (i in 0 until min(euroProofs.size, duplicateEuroProofs.size)) {
                if (euroProofs[i] == duplicateEuroProofs[i]) {
                    continue
                } else if (i > maxFirstDifferenceIndex) {
                    maxFirstDifferenceIndex = i
                    doubleSpendEuro = duplicateEuro
                    break
                }
            }
        }

        if (doubleSpendEuro != null) {
            val euroProof = euro.proofs[maxFirstDifferenceIndex]
            val depositProof = doubleSpendEuro.proofs[maxFirstDifferenceIndex]
            communicationProtocol.requestFraudControl(euro.serialNumber, euroProof, depositProof, "TTP")
            depositedEuroManager.insertDigitalEuro(euro)
            emitEvent("Double spending detected on deposit!")
            return "Deposit was successful!"
        }

        depositedEuroLogger.add(Pair(euro.serialNumber, FraudControlResult(true, null, null, null, null)))
        depositedEuroManager.insertDigitalEuro(euro)
        emitEvent("Noticed double spending but could not find a proof")
        return "Detected double spending but could not blame anyone"
    }

    fun depositFraudResult(serialNumber: String, dsResult: FraudControlResult) {
        if (dsResult.isFraud) {
            // Double spender detected
            depositedEuroLogger.add(Pair(serialNumber, dsResult))
            emitEvent(dsResult.toString())

            // PK and name of the double spender can be extracted from dsResult
            revealDoubleSpender(dsResult)
        }
    }

    private fun revealDoubleSpender(fraud: FraudControlResult) {
        if (fraud.isFraud) {
            if(!verifyRevealedCommitment(fraud.userPK!!, fraud.jwt!!, fraud.nonce!!))
                throw Exception("The revealed commitment does not match the stored one")
        }
    }

    fun getDepositedTokens(): List<DigitalEuro> {
        return depositedEuros
    }

    override fun onReceivedTransaction(
        transactionDetails: TransactionDetails,
        publicKeyBank: Element,
        publicKeySender: Element
    ): String {
        val ttpPublicKey = communicationProtocol.getPublicKeyOf("TTP", group)
        val transactionResult = Transaction.validate(transactionDetails, publicKeyBank, group, crs, ttpPublicKey)
        if (transactionResult.valid) {
            val digitalEuro = transactionDetails.digitalEuro
            digitalEuro.proofs.add(transactionDetails.currentTransactionProof.grothSahaiProof)
            return depositEuro(transactionDetails.digitalEuro, publicKeySender)
        }

        return transactionResult.description
    }

    override fun reset() {
        randomizationElementMap.clear()
        withdrawUserRandomness.clear()
        depositedEuroManager.clearDepositedEuros()
        commitmentManager.clearAllCommitments()
        setUp()
    }
}

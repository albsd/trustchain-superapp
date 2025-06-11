package nl.tudelft.trustchain.offlineeuro.entity

import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.ipv8.attestation.wallet.cryptography.bonehexact.generateRandomBigInteger
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.CRS
import nl.tudelft.trustchain.offlineeuro.cryptography.RandomizationElements
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import nl.tudelft.trustchain.offlineeuro.db.WalletManager
import java.math.BigInteger
import java.util.UUID

data class WalletEntry(
    val digitalEuro: DigitalEuro,
    val t: Element,
    val transactionSignature: SchnorrSignature?,
    val timesSpent: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletEntry
        return this.digitalEuro == other.digitalEuro &&
            this.t == other.t &&
            this.transactionSignature == other.transactionSignature &&
            this.timesSpent == other.timesSpent
    }
}

class Wallet(
    private val privateKey: Element,
    val publicKey: Element,
    private val walletManager: WalletManager
) {
    var signedPublicKey: SchnorrSignature? = null

    fun updateUserSignedPublicKey(signedPublicKey: SchnorrSignature) {
        this.signedPublicKey = signedPublicKey
    }

    fun addToWallet(
        transactionDetails: TransactionDetails,
        t: Element
    ) {
        val digitalEuro = transactionDetails.digitalEuro
        digitalEuro.proofs.add(transactionDetails.currentTransactionProof.grothSahaiProof)

        val transactionSignature = transactionDetails.theta1Signature
        val walletEntry = WalletEntry(digitalEuro, t, transactionSignature)
        walletManager.insertWalletEntry(walletEntry)
    }

    fun addToWallet(
        digitalEuro: DigitalEuro,
        t: Element
    ) {
        walletManager.insertWalletEntry(WalletEntry(digitalEuro, t, null))
    }

    fun getWalletEntryToSpend(): WalletEntry? {
        return walletManager.getNumberOfWalletEntriesToSpend(1).firstOrNull()
    }

    fun getAllWalletEntriesToSpend(): List<WalletEntry> {
        return walletManager.getWalletEntriesToSpend()
    }

    fun spendEuro(
        randomizationElements: RandomizationElements,
        bilinearGroup: BilinearGroup,
        crs: CRS,
        sn: String
    ): TransactionDetails? {
        val walletEntry = walletManager.getWalletEntriesBySerialNumber(sn).firstOrNull() ?: return null
        walletManager.removeWalletEntriesBySerialNumber(sn)
        return Transaction.createTransaction(privateKey, publicKey, signedPublicKey, walletEntry, randomizationElements, bilinearGroup, crs)
    }

    // ONLY FOR DEMO PURPOSES
    private fun generateRandomSignature(): SchnorrSignature {
        val upperBound = BigInteger("6666666")
        return SchnorrSignature(
            generateRandomBigInteger(upperBound),
            generateRandomBigInteger(upperBound),
            UUID.randomUUID().toString().toByteArray()
        )
    }

    // ONLY FOR DEMO PURPOSES
    fun generateWalletEntry(): WalletEntry {
        val group = BilinearGroup()
        val signature = generateRandomSignature()
        val randomDigitalEuro =
            DigitalEuro(
                UUID.randomUUID().toString(),
                group.generateRandomElementOfG(),
                generateRandomSignature(),
                arrayListOf()
            )

        return WalletEntry(randomDigitalEuro, group.getRandomZr(), signature)
    }

    fun doubleSpendEuro(
        randomizationElements: RandomizationElements,
        bilinearGroup: BilinearGroup,
        crs: CRS,
        sn: String
    ): TransactionDetails? {
        val walletEntry = walletManager.getWalletEntriesBySerialNumber(sn).firstOrNull() ?: return null
        return Transaction.createTransaction(privateKey, publicKey, signedPublicKey, walletEntry, randomizationElements, bilinearGroup, crs)
    }
}

package nl.tudelft.trustchain.offlineeuro.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.offlineeuro.sqldelight.BankCommitmentsQueries
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup

class BankCommitmentManager(
    context: Context?,
    private val bilinearGroup: BilinearGroup,
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context!!, "bank_commitments.db"),
) {
    private val database: Database = Database(driver)
    private val queries: BankCommitmentsQueries = database.bankCommitmentsQueries

    init {
        queries.createBankCommitmentTable()
    }

    fun storeCommitment(
        userPublicKey: Element,
        commitment: Element
    ) {
        queries.insertCommitment(
            userPublicKey.toBytes(),
            commitment.toBytes()
        )
    }

    fun getCommitmentByPublicKey(userPublicKey: Element): Element? {
        return queries.getCommitmentByPublicKey(userPublicKey.toBytes())
            .executeAsOneOrNull()
            ?.let { commitmentBytes ->
                bilinearGroup.pairing.g1.newElementFromBytes(commitmentBytes).immutable
            }
    }

    fun clearAllCommitments() {
        queries.clearAllCommitments()
    }
}

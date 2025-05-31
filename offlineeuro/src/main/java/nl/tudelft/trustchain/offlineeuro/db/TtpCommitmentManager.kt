package nl.tudelft.trustchain.offlineeuro.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.offlineeuro.sqldelight.TtpCommitmentsQueries
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup

class TtpCommitmentManager(
    context: Context?,
    private val bilinearGroup: BilinearGroup,
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context!!, "ttp_commitments.db"),
) {
    private val database: Database = Database(driver)
    private val queries: TtpCommitmentsQueries = database.ttpCommitmentsQueries

    init {
        queries.createTtpCommitmentTable()
    }

    fun storeCommitment(
        userPublicKey: Element,
        jwtToken: String,
        nonce: Element
    ) {
        queries.insertCommitment(
            userPublicKey.toBytes(),
            jwtToken,
            nonce.toBytes()
        )
    }

    fun getCommitmentByPublicKey(userPublicKey: Element): Pair<String, Element>? {
        return queries.getCommitmentByPublicKey(userPublicKey.toBytes()) { jwtToken, nonceBytes ->
            jwtToken to bilinearGroup.pairing.zr.newElementFromBytes(nonceBytes).immutable
        }.executeAsOneOrNull()
    }

    fun clearAllCommitments() {
        queries.clearAllCommitments()
    }
}

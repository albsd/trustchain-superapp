package nl.tudelft.trustchain.offlineeuro.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.offlineeuro.sqldelight.SignedPublicKeyQueries
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import nl.tudelft.trustchain.offlineeuro.libraries.SchnorrSignatureSerializer

class SignedPublicKeyManager(
    context: Context?,
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context!!, "signed_public_keys.db"),
) {
    private val database: Database = Database(driver)
    private val queries: SignedPublicKeyQueries = database.signedPublicKeyQueries

    init {
        queries.createSignedPublicKeyTable()
    }

    fun storeSignedPublicKey(signature: SchnorrSignature) {
        val serializedSignature = SchnorrSignatureSerializer.serializeSchnorrSignature(signature)
        queries.insertSignedPublicKey(serializedSignature)
    }

    fun getSignedPublicKey(): SchnorrSignature? {
        return queries.getSignedPublicKey()
            .executeAsOneOrNull()
            ?.let { signatureBytes ->
                SchnorrSignatureSerializer.deserializeSchnorrSignatureBytes(signatureBytes)
            }
    }

    fun clearSignedPublicKey() {
        queries.clearSignedPublicKey()
    }
}

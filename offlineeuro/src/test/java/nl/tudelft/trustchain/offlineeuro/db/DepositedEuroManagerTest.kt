package nl.tudelft.trustchain.offlineeuro.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import nl.tudelft.offlineeuro.sqldelight.Database
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.entity.DigitalEuro
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import it.unisa.dia.gas.jpbc.Element
import java.math.BigInteger

class DepositedEuroManagerTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val group = BilinearGroup()
    private lateinit var manager: DepositedEuroManager

    private fun createSampleDigitalEuro(serial: String): DigitalEuro {
        val g = group.g
        val theta = g.pow(BigInteger.valueOf(serial.hashCode().toLong()))
        val privateKey = group.getRandomZr()
        val signature = nl.tudelft.trustchain.offlineeuro.cryptography.Schnorr.schnorrSignature(
            privateKey = privateKey,
            message = serial.toByteArray(),
            group = group
        )
        return DigitalEuro(
            serialNumber = serial,
            firstTheta1 = theta,
            signature = signature,
            proofs = arrayListOf()
        )
    }

    @Before
    fun setUp() {
        manager = DepositedEuroManager(null, group, driver)
        manager.clearDepositedEuros()
    }

    @Test
    fun insertAndRetrieveDigitalEuro() {
        val digitalEuro = createSampleDigitalEuro("serial1")

        manager.insertDigitalEuro(digitalEuro)

        val allEuros = manager.getAllDepositedEuros()
        assertEquals(1, allEuros.size)

        val retrieved = allEuros[0]

        assertEquals(digitalEuro.serialNumber, retrieved.serialNumber)
        assertEquals(digitalEuro.firstTheta1, retrieved.firstTheta1)
        assertEquals(digitalEuro.signature, retrieved.signature)
        assertEquals(digitalEuro.proofs.size, retrieved.proofs.size)
    }

    @Test
    fun getDigitalEurosByDescriptorReturnsCorrectResults() {
        val digitalEuro1 = createSampleDigitalEuro("serial1")
        val digitalEuro2 = createSampleDigitalEuro("serial2")

        manager.insertDigitalEuro(digitalEuro1)
        manager.insertDigitalEuro(digitalEuro2)

        val results = manager.getDigitalEurosByDescriptor(digitalEuro1)
        assertEquals(1, results.size)
        assertEquals(digitalEuro1.serialNumber, results[0].serialNumber)
    }

    @Test
    fun getDigitalEurosByDescriptorReturnsEmptyIfNoMatch() {
        val digitalEuro = createSampleDigitalEuro("serial1")
        manager.insertDigitalEuro(digitalEuro)

        val nonMatching = createSampleDigitalEuro("not-in-db")

        val results = manager.getDigitalEurosByDescriptor(nonMatching)
        assertTrue(results.isEmpty())
    }

    @Test
    fun clearDepositedEurosRemovesAllEntries() {
        val digitalEuro = createSampleDigitalEuro("serial1")
        manager.insertDigitalEuro(digitalEuro)

        assertFalse(manager.getAllDepositedEuros().isEmpty())

        manager.clearDepositedEuros()
        assertTrue(manager.getAllDepositedEuros().isEmpty())
    }
}

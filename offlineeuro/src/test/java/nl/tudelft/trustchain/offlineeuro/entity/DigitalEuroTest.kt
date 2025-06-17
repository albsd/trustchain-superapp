package nl.tudelft.trustchain.offlineeuro.entity

import nl.tudelft.trustchain.offlineeuro.cryptography.*
import nl.tudelft.trustchain.offlineeuro.libraries.*
import org.junit.Assert.*
import org.junit.Test
import it.unisa.dia.gas.jpbc.Element
import nl.tudelft.trustchain.offlineeuro.cryptography.SchnorrSignature
import java.math.BigInteger

class DigitalEuroTest {

    private val group = BilinearGroup(PairingTypes.FromFile)

    private val g: Element = group.g
    private val h: Element = group.h
    private val theta1: Element = g.pow(BigInteger("10"))

    private val message = "sifon".toByteArray()
    private val privateKey: Element = group.getRandomZr()
    private val publicKey: Element = g.duplicate().powZn(privateKey)

    private val signature: SchnorrSignature = Schnorr.schnorrSignature(
        privateKey = privateKey,
        message = message,
        group = group
    )

    private val proof = GrothSahaiProof(
        c1 = g.duplicate(),
        c2 = h.duplicate(),
        d1 = g.duplicate(),
        d2 = h.duplicate(),
        theta1 = g.duplicate(),
        theta2 = h.duplicate(),
        pi1 = g.duplicate(),
        pi2 = h.duplicate(),
        target = g.duplicate()
    )

    private val digitalEuro = DigitalEuro(
        serialNumber = "sifon",
        firstTheta1 = theta1,
        signature = signature,
        proofs = arrayListOf(proof)
    )

    @Test
    fun toDigitalEuroBytes() {
        val bytes = digitalEuro.toDigitalEuroBytes()
        val restored = bytes.toDigitalEuro(group)

        assertEquals("sifon", restored.serialNumber)
        assertEquals(theta1, restored.firstTheta1)
        assertEquals(signature, restored.signature)
        assertEquals(digitalEuro.proofs.size, restored.proofs.size)

        digitalEuro.proofs.zip(restored.proofs).forEachIndexed { index, (original, restoredProof) ->
            val originalBytes = GrothSahaiSerializer.serializeGrothSahaiProof(original)
            val restoredBytes = GrothSahaiSerializer.serializeGrothSahaiProof(restoredProof)
            assertArrayEquals("Proof bytes differ at index $index", originalBytes, restoredBytes)
        }
    }

    @Test
    fun descriptorEquals() {
        val other = digitalEuro.copy()
        assertTrue(digitalEuro.descriptorEquals(other))
        assertEquals(digitalEuro, other)
    }

    @Test
    fun verifySignature() {
        assertTrue(digitalEuro.verifySignature(publicKey, group))
    }

    @Test
    fun sizeInBytes() {
        val serialBytes = digitalEuro.serialNumber.toByteArray()
        val thetaBytes = theta1.toBytes()
        val sigBytes = SchnorrSignatureSerializer.serializeSchnorrSignature(signature)
        val proofBytes = GrothSahaiSerializer.serializeGrothSahaiProofs(digitalEuro.proofs)!!

        val expected = serialBytes.size + thetaBytes.size + sigBytes.size + proofBytes.size
        assertEquals(expected, digitalEuro.sizeInBytes())
    }
}


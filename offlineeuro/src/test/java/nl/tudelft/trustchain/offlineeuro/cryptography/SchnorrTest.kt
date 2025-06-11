package nl.tudelft.trustchain.offlineeuro.cryptography

import org.junit.Assert
import org.junit.Test

class SchnorrTest {
    @Test
    fun signAndVerifyTest() {
        val group = BilinearGroup()
        val g = group.g
        val privateKey = group.getRandomZr()
        val publicKey = g.powZn(privateKey).immutable
        val elementToSign = g.powZn(group.getRandomZr())

        val schnorrSignature = Schnorr.schnorrSignature(privateKey, elementToSign.toBytes(), group)
        val verificationResult = Schnorr.verifySchnorrSignature(schnorrSignature, publicKey, group)
        Assert.assertTrue("The signature should be valid", verificationResult)
    }

    @Test
    fun blindSignAndVerifyTest() {
        val group = BilinearGroup()
        val g = group.g
        val privateKey = group.getRandomZr()
        val publicKey = g.powZn(privateKey).immutable
        val elementToSign = g.powZn(group.getRandomZr()).immutable.toBytes()
        val serialNumber = "TestSerialNumber"

        val bytesToSign = serialNumber.toByteArray() + elementToSign

        val k = group.getRandomZr()
        val r = g.powZn(k).immutable

        val blindedChallenge = Schnorr.createBlindedChallenge(r, bytesToSign, publicKey, group)
        val blindSignature = Schnorr.signBlindedChallenge(k, blindedChallenge.blindedChallenge, privateKey)
        val blindSchnorrSignature = Schnorr.unblindSignature(blindedChallenge, blindSignature)

        val verificationResult = Schnorr.verifySchnorrSignature(blindSchnorrSignature, publicKey, group)
        Assert.assertTrue("The signature should be valid", verificationResult)
    }

    @Test
    fun schnorrSignatureSerializationTest() {
        val group = BilinearGroup()
        val g = group.g
        val privateKey = group.getRandomZr()
        val publicKey = g.powZn(privateKey).immutable
        val elementToSign = g.powZn(group.getRandomZr())

        val originalSignature = Schnorr.schnorrSignature(privateKey, elementToSign.toBytes(), group)
        val serializedBytes = originalSignature.toBytes()
        val deserializedSignature = SchnorrSignature.fromBytes(serializedBytes)

        Assert.assertEquals("Signatures should be equal after serialization", originalSignature, deserializedSignature)
        Assert.assertTrue("Deserialized signature should be valid",
            Schnorr.verifySchnorrSignature(deserializedSignature, publicKey, group))
    }

    @Test
    fun schnorrSignatureEqualityTest() {
        val group = BilinearGroup()
        val g = group.g
        val privateKey = group.getRandomZr()
        val elementToSign = g.powZn(group.getRandomZr())

        val signature1 = Schnorr.schnorrSignature(privateKey, elementToSign.toBytes(), group)

        Assert.assertTrue("Signature should equal itself", signature1 == signature1)

        val signature1Copy = SchnorrSignature(
            signature1.signature,
            signature1.encryption,
            signature1.signedMessage.copyOf()
        )
        Assert.assertTrue("Signatures with same values should be equal", signature1 == signature1Copy)

        val differentSignature = Schnorr.schnorrSignature(privateKey, g.powZn(group.getRandomZr()).toBytes(), group)
        Assert.assertFalse("Signatures with different values should not be equal", signature1 == differentSignature)
    }
}

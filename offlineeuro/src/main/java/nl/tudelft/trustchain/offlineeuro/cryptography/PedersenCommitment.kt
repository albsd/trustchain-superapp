package nl.tudelft.trustchain.offlineeuro.cryptography

import it.unisa.dia.gas.jpbc.Element

/**
 * Utility functions for Pedersen commitments
 */
object PedersenCommitment {
    /**
     * Creates a Pedersen commitment
     */
    fun createCommitment(group: BilinearGroup, message: Element, nonce: Element): Element {
        val g = group.g
        val h = group.h
        return g.powZn(message).mul(h.powZn(nonce))
    }

    /**
     * Verifies a Pedersen commitment
     */
    fun verifyCommitment(
        group: BilinearGroup,
        commitment: Element,
        message: Element,
        nonce: Element
    ): Boolean {
        val expectedCommitment = createCommitment(group, message, nonce)
        return commitment == expectedCommitment
    }
}

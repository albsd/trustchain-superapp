package nl.tudelft.trustchain.offlineeuro.entity

import it.unisa.dia.gas.jpbc.Element

interface PublicKeyVerifier {
    fun isPublicKeyRegistered(publicKey: Element): Boolean
} 
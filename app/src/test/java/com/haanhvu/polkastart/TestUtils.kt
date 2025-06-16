package com.haanhvu.polkastart

import org.bitcoinj.core.Base58

fun generateSS58WestendPubkeyHexAndAddress(): Pair<String, String> {
    val pubKey = generatePublicKey()
    val pubKeyHex = pubKey.joinToString("") { "%02x".format(it) }
    val address = publicKeyToSS58Address(pubKey, 42)

    return Pair(pubKeyHex, address)
}

fun generatePublicKey(): ByteArray {
    val mnemonic = generateMnemonic()
    val keyPair = generateKeyPairFromMnemonic(mnemonic)

    return keyPair.second
}

fun extractPublicKeyFromAddress(address: String): ByteArray {
    val decoded = Base58.decode(address)
    return decoded.sliceArray(1..32)
}
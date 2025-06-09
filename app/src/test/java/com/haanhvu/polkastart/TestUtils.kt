package com.haanhvu.polkastart

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
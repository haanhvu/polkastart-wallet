package com.haanhvu.polkastart

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom
import java.security.Security

fun generateKeyPairFromMnemonic(mnemonic: String): Pair<ByteArray, ByteArray> {
    Security.addProvider(BouncyCastleProvider())

    val seed = mnemonicToSeed(mnemonic, "") // same as BIP39 seed
    val seed32 = seed.copyOfRange(0, 32) // Ed25519 requires 32 bytes

    val privKey = Ed25519PrivateKeyParameters(seed32, 0)
    val pubKey = privKey.generatePublicKey()

    return Pair(privKey.encoded, pubKey.encoded)
}

// Minimal BIP39-to-seed derivation (simplified)
fun mnemonicToSeed(mnemonic: String, passphrase: String): ByteArray {
    val salt = "mnemonic$passphrase"
    val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
    val spec = javax.crypto.spec.PBEKeySpec(
        mnemonic.toCharArray(),
        salt.toByteArray(),
        2048,
        512
    )
    val key = factory.generateSecret(spec)
    return key.encoded
}

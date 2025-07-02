package com.haanhvu.polkastart.crypto

import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom

fun generateMnemonic(): String {
    val entropy = ByteArray(16)
    SecureRandom().nextBytes(entropy)
    return MnemonicUtils.generateMnemonic(entropy)
}
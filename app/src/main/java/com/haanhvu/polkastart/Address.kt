package com.haanhvu.polkastart

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bitcoinj.core.Base58

fun publicKeyToSS58Address(pubKey: ByteArray, networkPrefix: Byte = 0): String {
    // 1. prefix + pubkey
    val addressBytes = ByteArray(1 + pubKey.size + 2) // prefix + pubkey + checksum(2 bytes)
    addressBytes[0] = networkPrefix
    System.arraycopy(pubKey, 0, addressBytes, 1, pubKey.size)

    // 2. calculate checksum
    val checksumInput = ByteArray(1 + pubKey.size)
    checksumInput[0] = networkPrefix
    System.arraycopy(pubKey, 0, checksumInput, 1, pubKey.size)

    val hash = blake2b512(checksumInput)

    // 3. first 2 bytes of hash are checksum
    addressBytes[addressBytes.size - 2] = hash[0]
    addressBytes[addressBytes.size - 1] = hash[1]

    // 4. base58 encode
    return Base58.encode(addressBytes)
}

fun blake2b512(input: ByteArray): ByteArray {
    val digest = Blake2bDigest(512)
    digest.update(input, 0, input.size)
    val out = ByteArray(64)
    digest.doFinal(out, 0)
    return out
}


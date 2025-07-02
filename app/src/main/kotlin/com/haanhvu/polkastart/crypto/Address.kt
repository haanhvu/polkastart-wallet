package com.haanhvu.polkastart.crypto

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bitcoinj.core.Base58

fun publicKeyToSS58Address(pubKey: ByteArray, networkPrefix: Byte = 0): String {
    // Step 1: Prepare address data (prefix + publicKey)
    val data = ByteArray(1 + pubKey.size)
    data[0] = networkPrefix
    System.arraycopy(pubKey, 0, data, 1, pubKey.size)

    // Step 2: Compute SS58 checksum using "SS58PRE" prefix
    val checksumInput = "SS58PRE".toByteArray() + data
    val hash = blake2b512(checksumInput)

    // Step 3: Combine prefix + pubKey + first 2 bytes of checksum
    val addressBytes = ByteArray(data.size + 2)
    System.arraycopy(data, 0, addressBytes, 0, data.size)
    addressBytes[addressBytes.size - 2] = hash[0]
    addressBytes[addressBytes.size - 1] = hash[1]

    // Step 4: Base58 encode full address
    return Base58.encode(addressBytes)
}

fun blake2b512(input: ByteArray): ByteArray {
    val digest = Blake2bDigest(512)
    digest.update(input, 0, input.size)
    val out = ByteArray(64)
    digest.doFinal(out, 0)
    return out
}


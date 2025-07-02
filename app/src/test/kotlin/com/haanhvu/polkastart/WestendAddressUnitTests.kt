package com.haanhvu.polkastart

import org.bitcoinj.core.Base58
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.junit.Test
import org.web3j.utils.Numeric

class WestendAddressUnitTests {
    @Test
    fun testGenerateSS58WestendAddress() {
        val pubKeyHexAndAddress = generateSS58WestendPubkeyHexAndAddress()
        checkSS58WestendAddress(pubKeyHexAndAddress.second, pubKeyHexAndAddress.first)
    }

    private fun checkSS58WestendAddress(address: String, expectedPublicKeyHex: String) {
        val decoded = Base58.decode(address)

        // Step 1: Check prefix (Westend = 42 decimal = 0x2A hex)
        val prefix = decoded[0].toInt() and 0xFF
        require(prefix == 42) {
            "Invalid SS58 prefix: expected 42, got $prefix"
        }

        // Step 2: Extract public key (next 32 bytes)
        val publicKeyBytes = decoded.sliceArray(1..32)
        val expectedBytes = Numeric.hexStringToByteArray(expectedPublicKeyHex)

        require(publicKeyBytes.contentEquals(expectedBytes)) {
            "Public key mismatch"
        }

        // Step 3: Check checksum for full SS58 validation
        val checksumFromAddress = decoded.sliceArray(33 until 35)
        val computedChecksum = calculateSs58Checksum(decoded[0].toByte(), publicKeyBytes)

        require(checksumFromAddress.contentEquals(computedChecksum)) {
            "Checksum mismatch"
        }
    }

    private fun calculateSs58Checksum(prefix: Byte, publicKey: ByteArray): ByteArray {
        val ss58Prefix = "SS58PRE".toByteArray()
        val input = ss58Prefix + byteArrayOf(prefix) + publicKey

        val digest = Blake2bDigest(512)
        digest.update(input, 0, input.size)
        val hash = ByteArray(64)
        digest.doFinal(hash, 0)

        return hash.copyOfRange(0, 2) // SS58 uses first 2 bytes of Blake2b-512 hash
    }
}
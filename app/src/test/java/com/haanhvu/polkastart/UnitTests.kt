package com.haanhvu.polkastart

import org.bitcoinj.core.Base58
import org.junit.Assert.*
import org.junit.Test
import org.web3j.utils.Numeric

class UnitTests {
    @Test
    fun testAddressSS58Westend() {
        val mnemonic = "common reunion close wrap fresh give make pattern wink huge edit text"
        val keyPair = generateKeyPairFromMnemonic(mnemonic)
        val pubKey = keyPair.second
        val pubKeyHex = pubKey.joinToString("") { "%02x".format(it) }
        val address = publicKeyToSS58Address(pubKey, 42)
        checkWestendAddress(address, pubKeyHex)
    }

    fun checkWestendAddress(address: String, expectedPublicKeyHex: String) {
        val decoded = Base58.decode(address)

        // Step 1: Check prefix (Westend = 42 decimal = 0x2A hex)
        val prefix = decoded[0].toInt() and 0xFF
        require(prefix == 42) { "Invalid SS58 prefix: expected 42, got $prefix" }

        // Step 2: Extract public key (next 32 bytes)
        val publicKeyBytes = decoded.sliceArray(1..32)
        val expectedBytes = Numeric.hexStringToByteArray(expectedPublicKeyHex)

        require(publicKeyBytes.contentEquals(expectedBytes)) {
            "Public key mismatch"
        }

        // Step 3: Check checksum for full SS58 validation?
    }
}
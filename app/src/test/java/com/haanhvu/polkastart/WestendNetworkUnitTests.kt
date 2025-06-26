package com.haanhvu.polkastart

import getAccountInfoThroughWebSocket
import org.junit.Test

class WestendNetworkUnitTests {
    @Test
    fun testConnectToWestend() {
        val testAddress = "5F7LB4iC4gXs1xkCJ4nAxCVdQSCW2RcThbX6y2EtcPYrFSxt"
        val pubKey = extractPublicKeyFromAddress(testAddress)
        println("Extracted pubKey: $pubKey")
        val realPubKey = hexToByteArray("0x86b572176b3c6d2268022811d16e28c7003e882b4438a5d5970b0705ad463c33")
        println("Real pubKey: $realPubKey")
        getAccountInfoThroughWebSocket(realPubKey) { balance ->
            if (balance != null) {
                println("Balance: $balance WND")
            } else {
                println("Failed to fetch balance")
            }
        }

        Thread.sleep(5000)
    }
}
package com.haanhvu.polkastart

import getAccountInfoThroughWebSocket
import org.junit.Test

class WestendNetworkUnitTests {
    @Test
    fun testConnectToWestend() {
        val testAddress = "5F7LB4iC4gXs1xkCJ4nAxCVdQSCW2RcThbX6y2EtcPYrFSxt"
        val pubKey = extractPublicKeyFromAddress(testAddress)
        getAccountInfoThroughWebSocket(pubKey) { balance ->
            if (balance != null) {
                println("Balance: $balance WND")
            } else {
                println("Failed to fetch balance")
            }
        }

        Thread.sleep(5000)
    }
}
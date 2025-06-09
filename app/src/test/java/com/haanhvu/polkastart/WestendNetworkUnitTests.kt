package com.haanhvu.polkastart

import getAccountInfoThroughWebSocket
import org.junit.Test

class WestendNetworkUnitTests {
    @Test
    fun testConnectToWestend() {
        val pubKey = generatePublicKey()
        getAccountInfoThroughWebSocket(pubKey) { balance ->
            if (balance != null) {
                println("🎉 Balance: $balance WND")
            } else {
                println("💥 Failed to fetch balance")
            }
        }

        Thread.sleep(5000)
    }
}
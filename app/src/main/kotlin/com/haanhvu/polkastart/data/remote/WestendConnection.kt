package com.haanhvu.polkastart.data.remote

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.jpountz.xxhash.XXHashFactory
import okhttp3.*
import org.bouncycastle.crypto.digests.Blake2bDigest

fun xxhash64(input: ByteArray, seed: Long): ByteArray {
    val factory = XXHashFactory.fastestInstance()
    val hasher = factory.hash64()
    val hash = hasher.hash(input, 0, input.size, seed)
    return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(hash).array()
}

fun twox128(input: ByteArray): ByteArray {
    return xxhash64(input, 0) + xxhash64(input, 1)
}

fun blake2b128(input: ByteArray): ByteArray {
    val digest = Blake2bDigest(128) // 128 bits = 16 bytes
    digest.update(input, 0, input.size)
    val output = ByteArray(16)
    digest.doFinal(output, 0)
    return output
}

fun blake2b128Concat(publicKey: ByteArray): ByteArray {
    val hashPart = blake2b128(publicKey)
    val fullHash = hashPart + publicKey
    return fullHash
}

fun getSystemAccountStorageKey(publicKey: ByteArray): String {
    val systemHash = twox128("System".toByteArray())
    val accountHash = twox128("Account".toByteArray())
    val publicKeyWithHash = blake2b128Concat(publicKey)

    val resultByte = systemHash + accountHash + publicKeyWithHash
    val resultHex = resultByte.joinToString("") { "%02x".format(it) }
    return "0x$resultHex"
}

fun getBalanceThroughWebSocket1(publicKey: ByteArray, onResult: (BigDecimal?) -> Unit) {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("wss://westend-asset-hub-rpc.polkadot.io")
        .build()

    val storageKey = getSystemAccountStorageKey(publicKey)

    val Json = Json { encodeDefaults = true }

    val requestJson = Json.encodeToString(
        RpcRequest(
            method = "state_getStorage",
            params = listOf(storageKey)
        )
    )

    val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WebSocket connected")
            println("Sent: $requestJson")
            webSocket.send(requestJson)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val response = json.decodeFromString<RpcResponse<String>>(text)

                var resultHex = response.result ?: throw Exception("No result field")

                if (!resultHex.startsWith("0x")) {
                    throw Exception("Invalid result hex format")
                }

                val resultBytes = resultHex.removePrefix("0x")
                    .chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()

                val free = resultBytes.sliceArray(16 until 32)

                val balanceInteger = java.math.BigInteger(1, free.reversedArray())
                val realBalance = BigDecimal(balanceInteger).movePointLeft(12)
                onResult(realBalance)
            } catch (e: Exception) {
                println("Failed to parse balance: ${e}")
                onResult(null)
            } finally {
                webSocket.close(1000, null)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WebSocket failure: ${t.message}")
            onResult(null)
        }
    }

    client.newWebSocket(request, listener)
}

fun getBalanceThroughWebSocket2(publicKey: ByteArray, onResult: (BigDecimal?) -> Unit) {
    val storageKey = getSystemAccountStorageKey(publicKey)

    WebSocketManager.sendRequest(
        method = "state_getStorage",
        params = listOf(storageKey)
    ) { text ->
        try {
            val json = Json { ignoreUnknownKeys = true }
            val response = json.decodeFromString<RpcResponse<String>>(text)
            var resultHex = response.result ?: throw Exception("No result field")

            if (!resultHex.startsWith("0x")) {
                throw Exception("Invalid result hex format")
            }

            val resultBytes = resultHex.removePrefix("0x")
                .chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

            val free = resultBytes.sliceArray(16 until 32)

            val balanceInteger = java.math.BigInteger(1, free.reversedArray())
            val realBalance = BigDecimal(balanceInteger).movePointLeft(12)
            onResult(realBalance)
        } catch (e: Exception) {
            println("Failed to parse balance: ${e}")
            onResult(null)
        } finally {
            WebSocketManager.close()
        }
    }
}

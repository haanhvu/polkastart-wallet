package com.haanhvu.polkastart.data.remote

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import net.jpountz.xxhash.XXHashFactory
import okhttp3.*
import org.bouncycastle.crypto.digests.Blake2bDigest

val Json = Json { encodeDefaults = true }

@Serializable
data class RpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: List<String>
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String
)

@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: RpcError? = null
)

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

fun getAccountInfoThroughWebSocket(publicKey: ByteArray, onResult: (BigDecimal?) -> Unit) {
    val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val request = Request.Builder()
        .url("wss://westend-asset-hub-rpc.polkadot.io")
        .build()

    val storageKey = getSystemAccountStorageKey(publicKey)

    val requestJson = Json.encodeToString(
        RpcRequest(
            method = "state_getStorage",
            params = listOf(storageKey)
        )
    )

    val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WebSocket connected")
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

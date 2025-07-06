package com.haanhvu.polkastart.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.math.BigDecimal

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
data class Params(
    val subscription: String = "",
    val result: List<List<String>> = emptyList()
)

@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val method: String = "",
    val params: Params = Params(),
    val result: T? = null,
    val error: RpcError? = null
)

object WebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private const val WS_URL = "wss://westend-asset-hub-rpc.polkadot.io"

    private val pendingCallbacks = mutableMapOf<Int, (String) -> Unit>()
    private var currentId = 1

    private var isConnected = false
    private val messageQueue = mutableListOf<String>()

    private var balanceUpdateListener: ((BigDecimal) -> Unit)? = null

    fun setBalanceUpdateListener(listener: (BigDecimal) -> Unit) {
        balanceUpdateListener = listener
    }

    fun connectIfNeeded() {
        if (webSocket != null && isConnected) return

        val request = Request.Builder().url(WS_URL).build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connected")
                isConnected = true
                messageQueue.forEach {
                    this@WebSocketManager.webSocket?.send(it)
                }
                messageQueue.clear()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = Json { ignoreUnknownKeys = true }
                    val response = json.decodeFromString<RpcResponse<String>>(text)
                    if (response.id != 0) {
                        pendingCallbacks.remove(response.id)?.invoke(text)
                    } else if (response.method == "state_storage") {
                        val resultHex = response.params.result[0][1]
                        try {
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

                            balanceUpdateListener?.invoke(realBalance)
                        } catch (e: Exception) {
                            println("Failed to parse balance: ${e}")
                        } finally {
                            WebSocketManager.close()
                        }
                    }
                } catch (e: Exception) {
                    println("Failed to handle message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket failed: ${t.message}")
                isConnected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closed: $reason")
                isConnected = false
                this@WebSocketManager.webSocket = null
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    fun sendRequest(
        method: String,
        params: List<String>,
        onResponse: (String) -> Unit
    ) {
        connectIfNeeded()
        val id = currentId++
        val request = buildJsonRpcRequest(id, method, params)
        pendingCallbacks[id] = onResponse

        if (isConnected) {
            webSocket?.send(request)
        } else {
            messageQueue.add(request)
        }
    }

    private fun buildJsonRpcRequest(id: Int, method: String, params: List<String>): String {
        val Json = Json { encodeDefaults = true }

        val requestJson = Json.encodeToString(
            RpcRequest(
                id = id,
                method = method,
                params = params
            )
        )

        return requestJson
    }

    fun close() {
        webSocket?.close(1000, null)
        webSocket = null
        isConnected = false
        balanceUpdateListener = null
    }
}

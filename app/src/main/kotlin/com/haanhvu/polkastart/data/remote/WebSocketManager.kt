package com.haanhvu.polkastart.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

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

object WebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private const val WS_URL = "wss://westend-asset-hub-rpc.polkadot.io"

    private val pendingCallbacks = mutableMapOf<Int, (String) -> Unit>()
    private var currentId = 1

    private var isConnected = false
    private val messageQueue = mutableListOf<String>()

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
                    } //else if (json.optString("method") == "state_storage") {
                        // Handle subscription notification
                        //println("Subscription event: $text")
                        // Route this via subscription ID if needed
                    //}
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
    }
}

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.math.BigDecimal

@Serializable
data class RpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: List<String>
)

@Serializable
data class AccountData(
    val free: String
)

@Serializable
data class AccountInfo(
    val nonce: Int,
    val data: AccountData
)

@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: JsonObject? = null
)

fun queryBalanceWebSocket(address: String, onResult: (BigDecimal?) -> Unit) {
    val uri = URI("wss://westend-rpc.polkadot.io")
    val requestJson = Json.encodeToString(RpcRequest(method = "system_account", params = listOf(address)))

    val client = object : WebSocketClient(uri) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            println("✅ WebSocket connected")
            send(requestJson)
        }

        override fun onMessage(message: String?) {
            println("📩 Response: $message")
            try {
                val json = Json.decodeFromString<RpcResponse<AccountInfo>>(message ?: "")
                val freeBalance = json.result?.data?.free
                val balance = freeBalance?.toBigDecimalOrNull()?.divide(BigDecimal("10000000000"))
                onResult(balance)
            } catch (e: Exception) {
                println("❌ Failed to parse balance: ${e.message}")
                onResult(null)
            } finally {
                close()
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            println("🔌 WebSocket closed: $reason")
        }

        override fun onError(ex: Exception?) {
            println("🚨 Error: ${ex?.message}")
            onResult(null)
        }
    }

    client.connect()
}

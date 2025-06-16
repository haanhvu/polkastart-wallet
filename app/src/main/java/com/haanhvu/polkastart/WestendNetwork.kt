import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.util.encoders.Hex

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

data class AccountInfo(
    val free: BigInteger,
    val reserved: BigInteger,
    val miscFrozen: BigInteger,
    val feeFrozen: BigInteger
)

fun blake2b128(input: ByteArray): ByteArray {
    val digest = Blake2bDigest(128) // 128 bits = 16 bytes
    digest.update(input, 0, input.size)
    val output = ByteArray(16)
    digest.doFinal(output, 0)
    return output
}

fun blake2b128Concat(publicKey: ByteArray): String {
    val hashPart = blake2b128(publicKey)
    val result = hashPart + publicKey
    return result.joinToString("") { "%02x".format(it) }
}

fun getSystemAccountStorageKey(publicKey: ByteArray): String {
    val hash = blake2b128Concat(publicKey)     // Step 2
    return "0x$hash"
}

fun decodeAccountInfo(bytes: ByteArray): AccountInfo {
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

    val free = buffer.long.toULong().toBigInteger()
    val reserved = buffer.long.toULong().toBigInteger()
    val miscFrozen = buffer.long.toULong().toBigInteger()
    val feeFrozen = buffer.long.toULong().toBigInteger()

    return AccountInfo(free, reserved, miscFrozen, feeFrozen)
}

fun getAccountInfoThroughWebSocket(publicKey: ByteArray, onResult: (BigDecimal?) -> Unit) {
    val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val request = Request.Builder()
        .url("wss://westend-rpc.polkadot.io")
        .build()

    val storageKey = getSystemAccountStorageKey(publicKey)
    println("🎉 Storage Key: " + storageKey)

    val requestJson = Json.encodeToString(
        RpcRequest(
            method = "state_getStorage",
            params = listOf(storageKey)
        )
    )

    println("Json serialization class = ${Json::class.qualifiedName}")
    println("📤 Sending JSON: $requestJson")

    val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("✅ WebSocket connected")
            webSocket.send(requestJson)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("📩 Response: $text")

            try {
                val json = Json { ignoreUnknownKeys = true }
                val response = json.decodeFromString<RpcResponse<String>>(text)

                val resultHex = response.result ?: throw Exception("No result field")

                if (!resultHex.startsWith("0x")) {
                    throw Exception("Invalid result hex format")
                }

                val rawBytes = Hex.decode(resultHex.removePrefix("0x"))
                val accountInfo = decodeAccountInfo(rawBytes)

                println("✅ Decoded AccountInfo: $accountInfo")


                val balance = BigDecimal(accountInfo.free.toString())
                    .divide(BigDecimal("1000000000000"))
                onResult(balance)
            } catch (e: Exception) {
                println("❌ Failed to parse balance: ${e.message}")
                onResult(null)
            } finally {
                webSocket.close(1000, null)
            }
        }


        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("🚨 WebSocket failure: ${t.message}")
            onResult(null)
        }
    }

    client.newWebSocket(request, listener)
}

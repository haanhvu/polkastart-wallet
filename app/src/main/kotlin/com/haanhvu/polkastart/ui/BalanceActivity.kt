package com.haanhvu.polkastart.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.haanhvu.polkastart.PolkaStartApp
import com.haanhvu.polkastart.data.local.balance.BalanceRepository
import org.bitcoinj.core.Base58

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: BalanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as PolkaStartApp
        val balanceDao = app.database.balanceDao()
        val repository = BalanceRepository(balanceDao)

        viewModel = BalanceViewModel(repository)
        val testAddress = "5F7LB4iC4gXs1xkCJ4nAxCVdQSCW2RcThbX6y2EtcPYrFSxt"
        val pubKey = extractPublicKeyFromAddress(testAddress)

        setContent {
            BalanceScreen(
                publicKey = pubKey,
                viewModel = viewModel
            )
        }
    }
}

fun extractPublicKeyFromAddress(address: String): ByteArray {
    val decoded = Base58.decode(address)
    return decoded.sliceArray(1..32)
}

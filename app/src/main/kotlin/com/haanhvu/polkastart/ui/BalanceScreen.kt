package com.haanhvu.polkastart.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun BalanceScreen(
    publicKey: ByteArray,
    viewModel: BalanceViewModel
) {
    val balance by viewModel.balanceState.collectAsState()

    LaunchedEffect(publicKey) {
        viewModel.observeBalance(publicKey)
        viewModel.subscribeToBalance(publicKey)
    }

    Text("Balance: ${balance?.toPlainString() ?: "Loading..."}")
}


package com.haanhvu.polkastart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haanhvu.polkastart.data.local.balance.BalanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BalanceViewModel(
    private val repository: BalanceRepository
) : ViewModel() {

    private val _balanceState = MutableStateFlow<BigDecimal?>(null)
    val balanceState: StateFlow<BigDecimal?> = _balanceState.asStateFlow()

    fun observeBalance(publicKey: ByteArray) {
        viewModelScope.launch {
            repository.observeBalance(publicKey).collect { entity ->
                _balanceState.value = entity?.free
            }
        }
    }

    fun subscribeToBalance(publicKey: ByteArray) {
        repository.subscribeToBalance(publicKey)
    }
}

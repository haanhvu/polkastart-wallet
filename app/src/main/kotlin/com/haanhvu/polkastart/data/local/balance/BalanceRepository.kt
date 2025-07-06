package com.haanhvu.polkastart.data.local.balance

import com.haanhvu.polkastart.data.remote.subscribeToBalance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BalanceRepository(
    private val dao: BalanceDao,
) {
    fun observeBalance(publicKey: ByteArray): Flow<BalanceEntity?> {
        return dao.observeBalance(publicKey)
    }

    fun subscribeToBalance(publicKey: ByteArray) {
        subscribeToBalance(publicKey) { balance ->
            val entity = BalanceEntity(
                publicKey = publicKey,
                free = balance!!,
            )
            CoroutineScope(Dispatchers.IO).launch {
                dao.insert(entity)
            }
        }
    }
}

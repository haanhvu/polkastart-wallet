package com.haanhvu.polkastart.data.local.balance

import kotlinx.coroutines.flow.Flow

class BalanceRepository(
    private val dao: BalanceDao,
    //private val substrateApi: SubstrateApi // WebSocket handler
) {
    fun observeBalance(accountId: String): Flow<BalanceEntity?> {
        return dao.observeBalance(accountId)
    }

    fun startBalanceSync(accountId: String) {
        /*substrateApi.subscribeAccountBalance(accountId) { balance ->
            val entity = BalanceEntity(
                accountId = accountId,
                free = balance.free,
                reserved = balance.reserved,
                miscFrozen = balance.miscFrozen,
                feeFrozen = balance.feeFrozen,
                timestamp = System.currentTimeMillis()
            )
            CoroutineScope(Dispatchers.IO).launch {
                dao.insert(entity)
            }
        }*/
    }
}

package com.haanhvu.polkastart.data.local.balance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balances WHERE publicKey = :publicKey")
    fun observeBalance(publicKey: ByteArray): Flow<BalanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(balance: BalanceEntity)
}
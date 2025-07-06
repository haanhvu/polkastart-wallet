package com.haanhvu.polkastart.data.local.balance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balances")
data class BalanceEntity(
    @PrimaryKey val accountId: String,
    val free: String,
    val reserved: String,
    val miscFrozen: String,
    val feeFrozen: String,
    val timestamp: Long
)

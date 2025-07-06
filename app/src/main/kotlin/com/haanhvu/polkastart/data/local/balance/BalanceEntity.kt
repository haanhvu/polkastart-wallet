package com.haanhvu.polkastart.data.local.balance

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "balances")
data class BalanceEntity(
    @PrimaryKey val publicKey: ByteArray,
    val free: BigDecimal,
    //val timestamp: Long
)

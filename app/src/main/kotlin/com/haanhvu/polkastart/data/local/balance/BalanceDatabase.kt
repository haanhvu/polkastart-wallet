package com.haanhvu.polkastart.data.local.balance

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BalanceEntity::class], version = 1)
abstract class BalanceDatabase : RoomDatabase() {
    abstract fun balanceDao(): BalanceDao
}

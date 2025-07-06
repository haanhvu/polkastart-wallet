package com.haanhvu.polkastart

import android.app.Application
import androidx.room.Room
import com.haanhvu.polkastart.data.local.balance.BalanceDao
import com.haanhvu.polkastart.data.local.balance.BalanceDatabase
import com.haanhvu.polkastart.data.local.balance.BalanceRepository

class PolkaStartApp: Application() {
    lateinit var database: BalanceDatabase
    lateinit var balanceDao: BalanceDao
    lateinit var balanceRepository: BalanceRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            BalanceDatabase::class.java,
            "balance-db"
        ).build()

        balanceDao = database.balanceDao()
        balanceRepository = BalanceRepository(balanceDao)
    }
}
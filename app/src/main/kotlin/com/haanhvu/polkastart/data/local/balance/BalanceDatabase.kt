package com.haanhvu.polkastart.data.local.balance

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.math.BigDecimal

@Database(
    entities = [BalanceEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class BalanceDatabase : RoomDatabase() {
    abstract fun balanceDao(): BalanceDao
}

class BigDecimalConverter {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? =
        value?.let { BigDecimal(it) }
}


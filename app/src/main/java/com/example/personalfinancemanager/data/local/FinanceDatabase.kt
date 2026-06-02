package com.example.personalfinancemanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.personalfinancemanager.data.model.Budget
import com.example.personalfinancemanager.data.model.Category
import com.example.personalfinancemanager.data.model.FinanceTransaction
import com.example.personalfinancemanager.data.model.SavingsGoal
import com.example.personalfinancemanager.data.model.TransactionType
import com.example.personalfinancemanager.data.model.UserProfile

class FinanceConverters {
    @TypeConverter fun toType(value: String) = TransactionType.valueOf(value)
    @TypeConverter fun fromType(value: TransactionType) = value.name
}

@Database(
    entities = [UserProfile::class, FinanceTransaction::class, Budget::class, SavingsGoal::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(FinanceConverters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile private var instance: FinanceDatabase? = null

        fun get(context: Context): FinanceDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                FinanceDatabase::class.java,
                "personal-finance.db"
            ).build().also { instance = it }
        }
    }
}

package com.example.personalfinancemanager.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.personalfinancemanager.data.model.Budget
import com.example.personalfinancemanager.data.model.Category
import com.example.personalfinancemanager.data.model.CategoryTotal
import com.example.personalfinancemanager.data.model.FinanceTransaction
import com.example.personalfinancemanager.data.model.SavingsGoal
import com.example.personalfinancemanager.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun observeProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeTransactions(): Flow<List<FinanceTransaction>>

    @Insert
    suspend fun addTransaction(transaction: FinanceTransaction)

    @Update
    suspend fun updateTransaction(transaction: FinanceTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransaction)

    @Query("SELECT * FROM budgets ORDER BY category")
    fun observeBudgets(): Flow<List<Budget>>

    @Insert
    suspend fun addBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM savings_goals ORDER BY targetDate")
    fun observeSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert
    suspend fun addSavingsGoal(goal: SavingsGoal)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)

    @Query("SELECT * FROM categories WHERE expense = 1 ORDER BY name")
    fun observeExpenseCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategories(categories: List<Category>)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavingsGoals()

    @Query("DELETE FROM user_profiles")
    suspend fun clearProfiles()
}

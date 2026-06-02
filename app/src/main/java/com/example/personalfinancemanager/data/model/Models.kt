package com.example.personalfinancemanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { INCOME, EXPENSE }

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val monthlyIncome: Double,
    val currency: String,
    val financialGoal: String,
    val darkMode: Boolean = false,
    val pin: String = ""
)

@Entity(tableName = "transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val description: String,
    val date: String,
    val paymentMethod: String,
    val recurring: Boolean = false,
    val billReminder: Boolean = false
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Double,
    val month: String
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String,
    val expense: Boolean = true
)

data class BudgetProgress(
    val budget: Budget,
    val spent: Double
) {
    val remaining get() = budget.amount - spent
    val ratio get() = if (budget.amount == 0.0) 0f else (spent / budget.amount).toFloat()
}

data class CategoryTotal(val category: String, val total: Double)

data class DashboardSummary(
    val balance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val savingsProgress: Float = 0f
)

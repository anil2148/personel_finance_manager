package com.example.personalfinancemanager.repository

import com.example.personalfinancemanager.data.local.FinanceDao
import com.example.personalfinancemanager.data.model.Budget
import com.example.personalfinancemanager.data.model.BudgetProgress
import com.example.personalfinancemanager.data.model.Category
import com.example.personalfinancemanager.data.model.CategoryTotal
import com.example.personalfinancemanager.data.model.DashboardSummary
import com.example.personalfinancemanager.data.model.FinanceTransaction
import com.example.personalfinancemanager.data.model.SavingsGoal
import com.example.personalfinancemanager.data.model.TransactionType
import com.example.personalfinancemanager.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FinanceRepository(private val dao: FinanceDao) {
    val profile = dao.observeProfile()
    val transactions = dao.observeTransactions()
    val budgets = dao.observeBudgets()
    val savingsGoals = dao.observeSavingsGoals()
    val expenseCategories = dao.observeExpenseCategories()

    fun summary(month: String): Flow<DashboardSummary> =
        combine(transactions, budgets, savingsGoals) { transactions, budgets, goals ->
            val monthly = transactions.filter { it.date.startsWith(month) }
            val monthlyIncome = monthly.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val monthlyExpenses = monthly.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val activeBudgets = budgets.filter { it.month == month }
            val budgetRemaining = activeBudgets.sumOf { budget ->
                budget.amount - monthly.filter { it.type == TransactionType.EXPENSE && it.category == budget.category }
                    .sumOf { it.amount }
            }
            val target = goals.sumOf { it.targetAmount }
            val saved = goals.sumOf { it.currentAmount }
            DashboardSummary(
                balance = totalIncome - totalExpenses,
                monthlyIncome = monthlyIncome,
                monthlyExpenses = monthlyExpenses,
                remainingBudget = budgetRemaining,
                savingsProgress = if (target == 0.0) 0f else (saved / target).toFloat()
            )
        }

    fun budgetProgress(month: String): Flow<List<BudgetProgress>> =
        combine(budgets, transactions) { budgets, transactions ->
            budgets.filter { it.month == month }.map { budget ->
                BudgetProgress(
                    budget,
                    transactions.filter {
                        it.type == TransactionType.EXPENSE && it.category == budget.category && it.date.startsWith(month)
                    }.sumOf { it.amount }
                )
            }
        }

    fun categoryTotals(month: String): Flow<List<CategoryTotal>> = transactions.map { items ->
        items.filter { it.type == TransactionType.EXPENSE && it.date.startsWith(month) }
            .groupBy { it.category }
            .map { CategoryTotal(it.key, it.value.sumOf(FinanceTransaction::amount)) }
            .sortedByDescending { it.total }
    }

    suspend fun seedCategories() = dao.addCategories(
        listOf("Food", "Rent", "Shopping", "Transport", "Bills", "Health", "Education",
            "Entertainment", "Travel", "Investment", "Other").map(::Category)
    )

    suspend fun saveProfile(profile: UserProfile) = dao.saveProfile(profile)
    suspend fun addTransaction(transaction: FinanceTransaction) = dao.addTransaction(transaction)
    suspend fun updateTransaction(transaction: FinanceTransaction) = dao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) = dao.deleteTransaction(transaction)
    suspend fun addBudget(budget: Budget) = dao.addBudget(budget)
    suspend fun deleteBudget(budget: Budget) = dao.deleteBudget(budget)
    suspend fun addSavingsGoal(goal: SavingsGoal) = dao.addSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    suspend fun reset() {
        dao.clearTransactions()
        dao.clearBudgets()
        dao.clearSavingsGoals()
        dao.clearProfiles()
    }
}

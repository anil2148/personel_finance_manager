package com.example.personalfinancemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personalfinancemanager.data.model.Budget
import com.example.personalfinancemanager.data.model.BudgetProgress
import com.example.personalfinancemanager.data.model.CategoryTotal
import com.example.personalfinancemanager.data.model.DashboardSummary
import com.example.personalfinancemanager.data.model.FinanceTransaction
import com.example.personalfinancemanager.data.model.SavingsGoal
import com.example.personalfinancemanager.data.model.UserProfile
import com.example.personalfinancemanager.repository.FinanceRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {
    val selectedMonth: String = YearMonth.now().toString()
    val today: String = LocalDate.now().toString()
    val profile = repository.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val savingsGoals = repository.savingsGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = repository.expenseCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val summary: StateFlow<DashboardSummary> = repository.summary(selectedMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardSummary())
    val budgetProgress: StateFlow<List<BudgetProgress>> = repository.budgetProgress(selectedMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categoryTotals: StateFlow<List<CategoryTotal>> = repository.categoryTotals(selectedMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.seedCategories() }
    }

    fun saveProfile(profile: UserProfile) = viewModelScope.launch { repository.saveProfile(profile) }
    fun addTransaction(transaction: FinanceTransaction) = viewModelScope.launch { repository.addTransaction(transaction) }
    fun updateTransaction(transaction: FinanceTransaction) = viewModelScope.launch { repository.updateTransaction(transaction) }
    fun deleteTransaction(transaction: FinanceTransaction) = viewModelScope.launch { repository.deleteTransaction(transaction) }
    fun addBudget(budget: Budget) = viewModelScope.launch { repository.addBudget(budget) }
    fun deleteBudget(budget: Budget) = viewModelScope.launch { repository.deleteBudget(budget) }
    fun addGoal(goal: SavingsGoal) = viewModelScope.launch { repository.addSavingsGoal(goal) }
    fun updateGoal(goal: SavingsGoal) = viewModelScope.launch { repository.updateSavingsGoal(goal) }
    fun deleteGoal(goal: SavingsGoal) = viewModelScope.launch { repository.deleteSavingsGoal(goal) }
    fun reset() = viewModelScope.launch { repository.reset() }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        FinanceViewModel(repository) as T
}

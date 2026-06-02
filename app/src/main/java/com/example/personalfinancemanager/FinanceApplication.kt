package com.example.personalfinancemanager

import android.app.Application
import com.example.personalfinancemanager.data.local.FinanceDatabase
import com.example.personalfinancemanager.repository.FinanceRepository

class FinanceApplication : Application() {
    val repository by lazy { FinanceRepository(FinanceDatabase.get(this).financeDao()) }
}

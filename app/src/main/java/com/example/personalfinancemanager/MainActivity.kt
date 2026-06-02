package com.example.personalfinancemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalfinancemanager.ui.FinanceApp
import com.example.personalfinancemanager.ui.theme.FinanceTheme
import com.example.personalfinancemanager.viewmodel.FinanceViewModel
import com.example.personalfinancemanager.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as FinanceApplication).repository
        setContent {
            val vm: FinanceViewModel = viewModel(factory = FinanceViewModelFactory(repository))
            val profile by vm.profile.collectAsState()
            FinanceTheme(darkMode = profile?.darkMode ?: false) {
                FinanceApp(vm)
            }
        }
    }
}

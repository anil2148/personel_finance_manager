package com.example.personalfinancemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.personalfinancemanager.data.model.Budget
import com.example.personalfinancemanager.data.model.BudgetProgress
import com.example.personalfinancemanager.data.model.CategoryTotal
import com.example.personalfinancemanager.data.model.DashboardSummary
import com.example.personalfinancemanager.data.model.FinanceTransaction
import com.example.personalfinancemanager.data.model.SavingsGoal
import com.example.personalfinancemanager.data.model.TransactionType
import com.example.personalfinancemanager.data.model.UserProfile
import com.example.personalfinancemanager.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.util.Currency

private val IncomeGreen = Color(0xFF2E7D32)
private val ExpenseRed = Color(0xFFC62828)
private val tabs = listOf(
    Tab("home", "Home", Icons.Default.Home),
    Tab("transactions", "Transactions", Icons.AutoMirrored.Filled.ReceiptLong),
    Tab("budgets", "Budget", Icons.Default.AccountBalanceWallet),
    Tab("reports", "Reports", Icons.Default.Assessment),
    Tab("profile", "Profile", Icons.Default.Person)
)

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun FinanceApp(vm: FinanceViewModel) {
    val profile by vm.profile.collectAsState()
    var unlocked by remember(profile?.pin) { mutableStateOf(profile?.pin.isNullOrBlank()) }
    if (profile == null) {
        OnboardingScreen(vm::saveProfile)
    } else if (!unlocked) {
        PinLockScreen(profile!!.pin) { unlocked = true }
    } else {
        MainScaffold(vm, profile!!)
    }
}

@Composable
private fun PinLockScreen(pin: String, onUnlock: () -> Unit) {
    var attempt by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    FormPage("PIN lock", "Enter your PIN to open Personal Finance Manager.") {
        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
        FormField(attempt, { attempt = it.filter(Char::isDigit).take(6) }, "PIN", numeric = true)
        ErrorText(error)
        Button({
            if (attempt == pin) onUnlock() else error = "Incorrect PIN"
        }, Modifier.fillMaxWidth()) { Text("Unlock") }
    }
}

@Composable
private fun MainScaffold(vm: FinanceViewModel, profile: UserProfile) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = { nav.navigate(tab.route) { launchSingleTop = true } },
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(vm, profile, nav) }
            composable("transactions") { TransactionsScreen(vm, profile.currency) }
            composable("budgets") { BudgetsScreen(vm, profile.currency) }
            composable("reports") { ReportsScreen(vm, profile.currency) }
            composable("profile") { ProfileScreen(vm, profile) }
            composable("premium") { PremiumScreen() }
        }
    }
}

@Composable
private fun OnboardingScreen(onSave: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var goal by remember { mutableStateOf("Save Money") }
    var error by remember { mutableStateOf("") }
    FormPage("Welcome", "Let's build your financial plan.") {
        FormField(name, { name = it }, "Your name")
        FormField(income, { income = it }, "Monthly income", numeric = true)
        ChoiceField("Currency", currency, listOf("USD", "INR", "EUR", "GBP")) { currency = it }
        ChoiceField("Main goal", goal, listOf("Save Money", "Reduce Expenses", "Track Spending", "Pay Debt", "Build Emergency Fund")) { goal = it }
        ErrorText(error)
        Button(
            onClick = {
                val amount = income.toDoubleOrNull()
                error = when {
                    name.isBlank() -> "Name is required"
                    amount == null || amount <= 0 -> "Monthly income must be greater than 0"
                    else -> ""
                }
                if (error.isEmpty()) onSave(UserProfile(name = name.trim(), monthlyIncome = amount!!, currency = currency, financialGoal = goal))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start managing money") }
    }
}

@Composable
private fun HomeScreen(vm: FinanceViewModel, profile: UserProfile, nav: NavHostController) {
    val summary by vm.summary.collectAsState()
    val goals by vm.savingsGoals.collectAsState()
    var transactionType by remember { mutableStateOf<TransactionType?>(null) }
    var showGoal by remember { mutableStateOf(false) }
    Page("Hi, ${profile.name}", "Your ${vm.selectedMonth} overview") {
        SummaryCard("Total balance", money(summary.balance, profile.currency), Icons.Default.AccountBalanceWallet)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallMetric("Income", money(summary.monthlyIncome, profile.currency), IncomeGreen, Modifier.weight(1f))
            SmallMetric("Expenses", money(summary.monthlyExpenses, profile.currency), ExpenseRed, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallMetric("Budget left", money(summary.remainingBudget, profile.currency), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            SmallMetric("Savings", "${(summary.savingsProgress * 100).toInt()}%", IncomeGreen, Modifier.weight(1f))
        }
        SectionTitle("Quick actions")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ transactionType = TransactionType.INCOME }, Modifier.weight(1f)) { Text("Add income") }
            OutlinedButton({ transactionType = TransactionType.EXPENSE }, Modifier.weight(1f)) { Text("Add expense") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ nav.navigate("budgets") }, Modifier.weight(1f)) { Text("Budget") }
            OutlinedButton({ nav.navigate("reports") }, Modifier.weight(1f)) { Text("Reports") }
        }
        SectionTitle("Savings goals")
        goals.forEach { SavingsGoalCard(it, profile.currency, vm::updateGoal, vm::deleteGoal) }
        OutlinedButton({ showGoal = true }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Text(" Add savings goal")
        }
        AdBanner()
    }
    transactionType?.let { type ->
        TransactionDialog(vm, type = type, onDismiss = { transactionType = null })
    }
    if (showGoal) GoalDialog(vm::addGoal) { showGoal = false }
}

@Composable
private fun TransactionsScreen(vm: FinanceViewModel, currency: String) {
    val transactions by vm.transactions.collectAsState()
    var editing by remember { mutableStateOf<FinanceTransaction?>(null) }
    var addType by remember { mutableStateOf<TransactionType?>(null) }
    Scaffold(
        floatingActionButton = { FloatingActionButton({ addType = TransactionType.EXPENSE }) { Icon(Icons.Default.Add, "Add transaction") } }
    ) { padding ->
        Page("Transactions", "Tap any entry to edit it", Modifier.padding(padding)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ addType = TransactionType.INCOME }, Modifier.weight(1f)) { Text("Add income") }
                Button({ addType = TransactionType.EXPENSE }, Modifier.weight(1f)) { Text("Add expense") }
            }
            transactions.forEach { item ->
                Card(onClick = { editing = item }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.category, fontWeight = FontWeight.Bold)
                            Text("${item.description.ifBlank { "No description" }} • ${item.date}")
                            if (item.recurring || item.billReminder) Text(listOfNotNull("Recurring".takeIf { item.recurring }, "Reminder".takeIf { item.billReminder }).joinToString(" • "), style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            "${if (item.type == TransactionType.INCOME) "+" else "-"}${money(item.amount, currency)}",
                            color = if (item.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    addType?.let { TransactionDialog(vm, type = it, onDismiss = { addType = null }) }
    editing?.let { TransactionDialog(vm, existing = it, type = it.type, onDismiss = { editing = null }) }
}

@Composable
private fun BudgetsScreen(vm: FinanceViewModel, currency: String) {
    val budgets by vm.budgetProgress.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    Page("Budgets", "Category limits for ${vm.selectedMonth}") {
        budgets.forEach { BudgetCard(it, currency, vm::deleteBudget) }
        Button({ showDialog = true }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Text(" Add budget")
        }
        AdBanner()
    }
    if (showDialog) BudgetDialog(vm, onDismiss = { showDialog = false })
}

@Composable
private fun ReportsScreen(vm: FinanceViewModel, currency: String) {
    val summary by vm.summary.collectAsState()
    val categories by vm.categoryTotals.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val dailyTotals = transactions
        .filter { it.type == TransactionType.EXPENSE && it.date.startsWith(vm.selectedMonth) }
        .groupBy { it.date }
        .map { CategoryTotal(it.key, it.value.sumOf(FinanceTransaction::amount)) }
        .sortedBy { it.category }
    Page("Reports", "Monthly analytics for ${vm.selectedMonth}") {
        SummaryCard("Income vs expense", "${money(summary.monthlyIncome, currency)}  /  ${money(summary.monthlyExpenses, currency)}", Icons.Default.Assessment)
        SectionTitle("Category breakdown")
        CategoryBars(categories, currency)
        SectionTitle("Daily spending trend")
        CategoryBars(dailyTotals, currency)
        SectionTitle("Top spending categories")
        categories.take(3).forEachIndexed { index, total -> Text("${index + 1}. ${total.category}: ${money(total.total, currency)}") }
        AdBanner()
    }
}

@Composable
private fun ProfileScreen(vm: FinanceViewModel, profile: UserProfile) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var income by remember(profile) { mutableStateOf(profile.monthlyIncome.toString()) }
    var currency by remember(profile) { mutableStateOf(profile.currency) }
    var pin by remember(profile) { mutableStateOf(profile.pin) }
    var dark by remember(profile) { mutableStateOf(profile.darkMode) }
    var showPremium by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    Page("Profile", "Preferences and data controls") {
        FormField(name, { name = it }, "Name")
        FormField(income, { income = it }, "Monthly income", numeric = true)
        ChoiceField("Currency", currency, listOf("USD", "INR", "EUR", "GBP")) { currency = it }
        FormField(pin, { pin = it.filter(Char::isDigit).take(6) }, "PIN lock (optional)", numeric = true)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Dark mode", Modifier.weight(1f))
            Switch(dark, { dark = it })
        }
        ErrorText(status)
        Button({
            val parsed = income.toDoubleOrNull()
            status = if (name.isBlank() || parsed == null || parsed <= 0) "Enter a valid name and income" else {
                vm.saveProfile(profile.copy(name = name.trim(), monthlyIncome = parsed, currency = currency, pin = pin, darkMode = dark))
                "Profile saved"
            }
        }, Modifier.fillMaxWidth()) { Text("Save profile") }
        OutlinedButton({ status = "CSV export is coming soon." }, Modifier.fillMaxWidth()) { Text("Export data (CSV placeholder)") }
        OutlinedButton({ showPremium = true }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.WorkspacePremium, null)
            Text(" Explore Premium")
        }
        TextButton({ vm.reset() }, Modifier.fillMaxWidth()) { Text("Reset all data", color = ExpenseRed) }
    }
    if (showPremium) AlertDialog(
        onDismissRequest = { showPremium = false },
        confirmButton = { TextButton({ showPremium = false }) { Text("Close") } },
        title = { Text("Personal Finance Premium") },
        text = { PremiumBenefits() }
    )
}

@Composable
private fun PremiumScreen() = Page("Premium", "Unlock your complete finance toolkit") { PremiumBenefits() }

@Composable
private fun PremiumBenefits() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Advanced reports", "Unlimited budgets", "Cloud backup", "Export to PDF / Excel", "No ads").forEach { Text("✓ $it") }
        Text("Premium purchase integration will be added in a future release.")
    }
}

@Composable
private fun TransactionDialog(vm: FinanceViewModel, type: TransactionType, existing: FinanceTransaction? = null, onDismiss: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: categories.firstOrNull()?.name.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var date by remember { mutableStateOf(existing?.date ?: vm.today) }
    var payment by remember { mutableStateOf(existing?.paymentMethod ?: "Cash") }
    var recurring by remember { mutableStateOf(existing?.recurring ?: false) }
    var reminder by remember { mutableStateOf(existing?.billReminder ?: false) }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add ${type.name.lowercase()}" else "Edit transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField(amount, { amount = it }, "Amount", numeric = true)
                ChoiceField("Category", category, if (type == TransactionType.INCOME) listOf("Salary", "Investment", "Other") else categories.map { it.name }) { category = it }
                FormField(description, { description = it }, "Description")
                FormField(date, { date = it }, "Date (YYYY-MM-DD)")
                ChoiceField("Payment", payment, listOf("Cash", "Credit Card", "Debit Card", "Bank Transfer", "UPI")) { payment = it }
                CheckRow("Recurring transaction", recurring) { recurring = it }
                CheckRow("Bill reminder", reminder) { reminder = it }
                ErrorText(error)
            }
        },
        confirmButton = {
            TextButton({
                val parsed = amount.toDoubleOrNull()
                error = if (parsed == null || parsed <= 0 || category.isBlank() || date.isBlank()) "Enter an amount, category, and date" else ""
                if (error.isEmpty()) {
                    val item = FinanceTransaction(existing?.id ?: 0, parsed!!, type, category, description.trim(), date, payment, recurring, reminder)
                    if (existing == null) vm.addTransaction(item) else vm.updateTransaction(item)
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                existing?.let { TextButton({ vm.deleteTransaction(it); onDismiss() }) { Text("Delete", color = ExpenseRed) } }
                TextButton(onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun BudgetDialog(vm: FinanceViewModel, onDismiss: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var category by remember { mutableStateOf(categories.firstOrNull()?.name.orEmpty()) }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add monthly budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceField("Category", category, categories.map { it.name }) { category = it }
                FormField(amount, { amount = it }, "Budget amount", numeric = true)
                ErrorText(error)
            }
        },
        confirmButton = { TextButton({
            val parsed = amount.toDoubleOrNull()
            error = if (category.isBlank() || parsed == null || parsed <= 0) "Choose a category and enter an amount" else ""
            if (error.isEmpty()) { vm.addBudget(Budget(category = category, amount = parsed!!, month = vm.selectedMonth)); onDismiss() }
        }) { Text("Save") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun GoalDialog(onSave: (SavingsGoal) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("0") }
    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add savings goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField(name, { name = it }, "Goal name")
                FormField(target, { target = it }, "Target amount", numeric = true)
                FormField(saved, { saved = it }, "Current saved amount", numeric = true)
                FormField(date, { date = it }, "Target date (YYYY-MM-DD)")
                ErrorText(error)
            }
        },
        confirmButton = { TextButton({
            val targetValue = target.toDoubleOrNull()
            val savedValue = saved.toDoubleOrNull()
            error = if (name.isBlank() || date.isBlank() || targetValue == null || targetValue <= 0 || savedValue == null || savedValue < 0) "Complete all required fields with valid amounts" else ""
            if (error.isEmpty()) { onSave(SavingsGoal(name = name.trim(), targetAmount = targetValue!!, currentAmount = savedValue!!, targetDate = date)); onDismiss() }
        }) { Text("Save") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SavingsGoalCard(goal: SavingsGoal, currency: String, onUpdate: (SavingsGoal) -> Unit, onDelete: (SavingsGoal) -> Unit) {
    var add by remember { mutableStateOf("") }
    val ratio = if (goal.targetAmount == 0.0) 0f else (goal.currentAmount / goal.targetAmount).toFloat()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Text(goal.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("${(ratio * 100).toInt()}%")
            }
            LinearProgressIndicator({ ratio.coerceIn(0f, 1f) }, Modifier.fillMaxWidth())
            Text("${money(goal.currentAmount, currency)} of ${money(goal.targetAmount, currency)} • ${goal.targetDate}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FormField(add, { add = it }, "Add savings", numeric = true, modifier = Modifier.weight(1f))
                TextButton({ add.toDoubleOrNull()?.takeIf { it > 0 }?.let { onUpdate(goal.copy(currentAmount = goal.currentAmount + it)); add = "" } }) { Text("Update") }
                IconButton({ onDelete(goal) }) { Text("×", color = ExpenseRed) }
            }
        }
    }
}

@Composable
private fun BudgetCard(progress: BudgetProgress, currency: String, onDelete: (Budget) -> Unit) {
    val color = when {
        progress.ratio > 1f -> ExpenseRed
        progress.ratio >= .8f -> Color(0xFFF57C00)
        else -> IncomeGreen
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row {
                Text(progress.budget.category, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                IconButton({ onDelete(progress.budget) }) { Text("×", color = ExpenseRed) }
            }
            LinearProgressIndicator({ progress.ratio.coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = color)
            Text("${money(progress.spent, currency)} used • ${money(progress.remaining, currency)} remaining", color = color)
            if (progress.ratio > 1f) Text("Budget exceeded", color = ExpenseRed)
            else if (progress.ratio >= .8f) Text("You have used over 80% of this budget", color = color)
        }
    }
}

@Composable
private fun CategoryBars(items: List<CategoryTotal>, currency: String) {
    val highest = items.maxOfOrNull { it.total } ?: 1.0
    if (items.isEmpty()) Text("Add expenses to see your category breakdown.")
    items.forEach {
        Text("${it.category} • ${money(it.total, currency)}", fontWeight = FontWeight.Medium)
        LinearProgressIndicator({ (it.total / highest).toFloat() }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun SummaryCard(title: String, value: String, icon: ImageVector) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.padding(end = 12.dp))
            Column {
                Text(title)
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SmallMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp)) { Text(title); Text(value, color = color, fontWeight = FontWeight.Bold) } }
}

@Composable
private fun AdBanner() {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text("Ad banner placeholder", Modifier.padding(16.dp).align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun Page(title: String, subtitle: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
    }
}

@Composable
private fun FormPage(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(20.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(subtitle)
                content()
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

@Composable
private fun FormField(value: String, onValueChange: (String) -> Unit, label: String, numeric: Boolean = false, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text))
}

@Composable
private fun ChoiceField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton({ expanded = true }, Modifier.fillMaxWidth()) { Text("$label: ${value.ifBlank { "Choose" }}") }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { option -> DropdownMenuItem({ Text(option) }, { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked, onChange); Text(label) }
}

@Composable
private fun ErrorText(error: String) {
    if (error.isNotEmpty()) Text(error, color = if (error.contains("saved") || error.contains("coming")) IncomeGreen else ExpenseRed)
}

private fun money(amount: Double, currency: String): String = runCatching {
    NumberFormat.getCurrencyInstance().apply { this.currency = Currency.getInstance(currency) }.format(amount)
}.getOrElse { "$currency ${"%.2f".format(amount)}" }

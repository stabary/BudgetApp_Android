package com.simon.budgetapp.ui.budgetdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simon.budgetapp.network.Transaction
import androidx.compose.material.icons.filled.PieChart
import com.simon.budgetapp.ui.components.PieChartView
import com.simon.budgetapp.ui.components.PieSlice
import androidx.compose.foundation.background
import com.simon.budgetapp.ui.components.BarChartView
import com.simon.budgetapp.ui.components.BarGroup
import java.text.SimpleDateFormat as SimpleDateFormatJava
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Group
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.BoxWithConstraints
private val chartColorsList = listOf(
    Color(0xFFEF5350), Color(0xFFFFA726), Color(0xFFFFEE58),
    Color(0xFF66BB6A), Color(0xFF26C6DA), Color(0xFF42A5F5),
    Color(0xFF7E57C2), Color(0xFFEC407A), Color(0xFF8D6E63),
    Color(0xFF78909C)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    budgetId: Int,
    onBack: () -> Unit,
    onNavigateToStats: (Int) -> Unit,
    onNavigateToRecurring: (Int) -> Unit,
    onNavigateToSharing: (Int) -> Unit,
    onNavigateToCategoryDetail: (Int) -> Unit,   // <-- nouveau
    viewModel: BudgetDetailViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(budgetId) {
        viewModel.loadData(budgetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.balance?.budget_name ?: "Détail du budget") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToStats(budgetId) }) {
                        Icon(Icons.Default.PieChart, contentDescription = "Statistiques")
                    }
                    IconButton(onClick = { onNavigateToRecurring(budgetId) }) {
                        Icon(Icons.Default.Repeat, contentDescription = "Routines récurrentes")
                    }
                    IconButton(onClick = { onNavigateToSharing(budgetId) }) {
                        Icon(Icons.Default.Group, contentDescription = "Partage")
                    }
                    val context = LocalContext.current

                    IconButton(onClick = {
                        viewModel.exportToCSV(budgetId) { uri ->
                            uri?.let {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, it)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Exporter vers..."))
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Exporter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter une transaction",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                viewModel.transactions.isEmpty() && viewModel.monthlyBalance == null -> {
                    Text(
                        text = "Aucune transaction.\nAjoute ta première dépense ou entrée !",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {

                        // Carte de solde
                        viewModel.monthlyBalance?.let { monthly ->
                            item {
                                val balanceColor = when (monthly.status) {
                                    "danger" -> Color(0xFFC62828)
                                    "warning" -> Color(0xFFF57C00)
                                    else -> Color(0xFF2E7D32)
                                }

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    val isTablet = maxWidth > 600.dp

                                    val monthlyCard: @Composable () -> Unit = {
                                        Card(
                                            onClick = { onNavigateToCategoryDetail(budgetId) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Solde du mois : ${"%.2f".format(monthly.balance)} €",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = balanceColor
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Entrées : ${"%.2f".format(monthly.total_income)} € · Dépenses : ${"%.2f".format(monthly.total_expense)} €")
                                            }
                                        }
                                    }

                                    val accountCard: @Composable () -> Unit = {
                                        viewModel.accountBalance?.let { account ->
                                            Card(modifier = Modifier.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    val accountColor = if (account.account_balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)

                                                    Text(
                                                        text = "Solde de compte : ${"%.2f".format(account.account_balance)} €",
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        color = accountColor
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        text = "Entrées globales : ${"%.2f".format(account.actual_income)} €",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Dépenses globales : ${"%.2f".format(account.actual_expense)} €",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (isTablet) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) { accountCard() }
                                            Box(modifier = Modifier.weight(1f)) { monthlyCard() }
                                        }
                                    } else {
                                        Column {
                                            monthlyCard()
                                            Spacer(modifier = Modifier.height(8.dp))
                                            accountCard()
                                        }
                                    }
                                }
                            }
                        }

                        // Pie chart
                        if (viewModel.currentMonthCategories.isNotEmpty()) {
                            item {
                                val total = viewModel.currentMonthCategories.sumOf { it.total.toDoubleOrNull() ?: 0.0 }
                                val slices = viewModel.currentMonthCategories.mapIndexed { index, item ->
                                    PieSlice(
                                        label = item.category_name ?: "Autres",
                                        value = item.total.toDoubleOrNull() ?: 0.0,
                                        color = chartColorsList[index % chartColorsList.size]
                                    )
                                }

                                Column {
                                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PieChartView(slices = slices)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text("Dépenses du mois", style = MaterialTheme.typography.titleSmall)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                slices.take(5).forEach { slice ->
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .background(slice.color, shape = androidx.compose.foundation.shape.CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            "${slice.label} (${(slice.value / total * 100).toInt()}%)",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Bar chart historique
                        if (viewModel.monthlyHistory.isNotEmpty()) {
                            item {
                                val monthFormatter = remember { java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()) }
                                val labelFormatter = remember { java.text.SimpleDateFormat("MMM", java.util.Locale("fr", "FR")) }

                                val groups = viewModel.monthlyHistory.map { history ->
                                    val label = try {
                                        labelFormatter.format(monthFormatter.parse(history.month)!!)
                                    } catch (e: Exception) {
                                        history.month
                                    }
                                    BarGroup(
                                        label = label,
                                        income = history.total_income.toDoubleOrNull() ?: 0.0,
                                        expense = history.total_expense.toDoubleOrNull() ?: 0.0
                                    )
                                }

                                Column {
                                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Historique (6 derniers mois)", style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            BarChartView(groups = groups)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // À venir ce mois
                        if (viewModel.upcomingRules.isNotEmpty()) {
                            item {
                                Column {
                                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("À venir ce mois", style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            viewModel.upcomingRules.forEach { rule ->
                                                val isExpense = rule.type == "expense"
                                                val amount = rule.amount.toDoubleOrNull() ?: 0.0
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        "${rule.label} (prévu le ${rule.next_run_date.takeLast(2)})",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                    )
                                                    Text(
                                                        "${if (isExpense) "-" else "+"}${"%.2f".format(amount)} €",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                        color = if (isExpense) Color(0xFFC62828).copy(alpha = 0.7f) else Color(0xFF2E7D32).copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Liste des transactions
                        items(viewModel.transactions) { tx ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                TransactionRow(tx, onClick = { transactionToEdit = tx })
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            categories = viewModel.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { categoryId, type, amount, label, date, frequency ->
                if (frequency == "none") {
                    viewModel.addTransaction(budgetId, categoryId, type, amount, label, date) {
                        showAddDialog = false
                    }
                } else {
                    val dayOfMonth = if (frequency == "monthly") {
                        date.split("-").lastOrNull()?.toIntOrNull()
                    } else null
                    viewModel.addRecurringTransaction(
                        budgetId, categoryId, label, amount, type, frequency, dayOfMonth, date
                    ) {
                        showAddDialog = false
                    }
                }
            }
        )
    }

    transactionToEdit?.let { tx ->
        AddTransactionDialog(
            categories = viewModel.categories,
            existingTransaction = tx,
            onDismiss = { transactionToEdit = null },
            onConfirm = { categoryId, type, amount, label, date, _ ->
                viewModel.updateTransaction(tx.id, budgetId, categoryId, type, amount, label, date) {
                    transactionToEdit = null
                }
            },
            onDelete = {
                viewModel.deleteTransaction(tx.id, budgetId) {
                    transactionToEdit = null
                }
            }
        )
    }
}


@Composable
fun TransactionRow(transaction: Transaction, onClick: () -> Unit) {
    val isExpense = transaction.type == "expense"
    val amountValue = transaction.amount.toDoubleOrNull() ?: 0.0

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = transaction.label, style = MaterialTheme.typography.titleMedium)
                Text(text = transaction.transaction_date, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${if (isExpense) "-" else "+"}${"%.2f".format(amountValue)} €",
                color = if (isExpense) Color(0xFFC62828) else Color(0xFF2E7D32),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
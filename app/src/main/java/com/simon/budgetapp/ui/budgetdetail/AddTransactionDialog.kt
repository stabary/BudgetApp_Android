package com.simon.budgetapp.ui.budgetdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simon.budgetapp.network.Category
import com.simon.budgetapp.network.Transaction
import com.simon.budgetapp.ui.components.DatePickerField
import java.text.SimpleDateFormat
import java.util.*

private val transactionFrequencies = listOf(
    "none" to "Ponctuel",
    "monthly" to "Mensuel",
    "weekly" to "Hebdomadaire",
    "yearly" to "Annuel",
    "daily" to "Quotidien"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    existingTransaction: Transaction? = null, // null = création, non-null = édition
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Int?, type: String, amount: Double, label: String, date: String, frequency: String) -> Unit,
    onDelete: (() -> Unit)? = null // affiché uniquement en mode édition
) {
    var label by remember { mutableStateOf(existingTransaction?.label ?: "") }
    var amountText by remember {
        mutableStateOf(existingTransaction?.amount?.replace(".", ",") ?: "")
    }
    var type by remember { mutableStateOf(existingTransaction?.type ?: "expense") }
    var selectedCategoryId by remember { mutableStateOf(existingTransaction?.category_id) }

    var selectedDate by remember {
        mutableStateOf(
            existingTransaction?.transaction_date
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    // Fréquence : uniquement pertinent en création
    var frequency by remember { mutableStateOf("none") }
    var frequencyExpanded by remember { mutableStateOf(false) }
    val isRecurring = existingTransaction == null && frequency != "none"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTransaction == null) "Nouvelle transaction" else "Modifier la transaction") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = type == "expense",
                        onClick = { type = "expense" },
                        label = { Text("Dépense") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = type == "income",
                        onClick = { type = "income" },
                        label = { Text("Entrée") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Libellé") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant (€)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (existingTransaction == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = frequencyExpanded,
                        onExpandedChange = { frequencyExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = transactionFrequencies.find { it.first == frequency }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fréquence") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = frequencyExpanded,
                            onDismissRequest = { frequencyExpanded = false }
                        ) {
                            transactionFrequencies.forEach { (value, displayLabel) ->
                                DropdownMenuItem(
                                    text = { Text(displayLabel) },
                                    onClick = {
                                        frequency = value
                                        frequencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                DatePickerField(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    label = if (isRecurring) "Date de début" else "Date"
                )

                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredCategories = categories.filter { it.type == type }
                    var expanded by remember { mutableStateOf(false) }
                    val selectedCategoryName = filteredCategories.find { it.id == selectedCategoryId }?.name ?: "Aucune"

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Aucune") },
                                onClick = {
                                    selectedCategoryId = null
                                    expanded = false
                                }
                            )
                            filteredCategories.groupBy { it.group_name ?: "Autres" }.forEach { (group, catsInGroup) ->
                                HorizontalDivider()
                                Text(
                                    text = group,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                                )
                                catsInGroup.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (existingTransaction != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onDelete) {
                        Text("Supprimer cette transaction", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull()
                    if (amount != null && label.isNotBlank()) {
                        onConfirm(selectedCategoryId, type, amount, label, selectedDate, frequency)
                    }
                },
                enabled = label.isNotBlank() && amountText.isNotBlank()
            ) { Text(if (existingTransaction == null) "Ajouter" else "Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
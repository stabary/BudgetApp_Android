package com.simon.budgetapp.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simon.budgetapp.network.Category
import com.simon.budgetapp.ui.components.DatePickerField
import java.text.SimpleDateFormat
import java.util.*

private val frequencies = listOf("monthly" to "Mensuel", "weekly" to "Hebdomadaire", "yearly" to "Annuel", "daily" to "Quotidien")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringRuleDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Int?, label: String, amount: Double, type: String, frequency: String, dayOfMonth: Int?, startDate: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var frequency by remember { mutableStateOf("monthly") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var startDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle routine") },
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
                    label = { Text("Libellé (ex: Netflix)") },
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
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = frequencies.find { it.first == frequency }?.second ?: "",
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
                        frequencies.forEach { (value, displayLabel) ->
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

                Spacer(modifier = Modifier.height(8.dp))
                DatePickerField(
                    selectedDate = startDate,
                    onDateSelected = { startDate = it },
                    label = "Date de début"
                )

                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val filteredCategories = categories.filter { it.type == type }
                    val selectedName = filteredCategories.find { it.id == selectedCategoryId }?.name ?: "Aucune"

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Aucune") },
                                onClick = { selectedCategoryId = null; categoryExpanded = false }
                            )
                            filteredCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                                )
                            }
                        }
                    }
                }
                if (selectedCategoryId == null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sélectionne une catégorie (ex: Abonnement)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.replace(",", ".").toDoubleOrNull()
                    if (amount != null && label.isNotBlank()) {
                        val dayOfMonth = if (frequency == "monthly") {
                            startDate.split("-").lastOrNull()?.toIntOrNull()
                        } else null
                        onConfirm(selectedCategoryId, label, amount, type, frequency, dayOfMonth, startDate)
                    }
                },
                enabled = label.isNotBlank() && amountText.isNotBlank() && selectedCategoryId != null
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}


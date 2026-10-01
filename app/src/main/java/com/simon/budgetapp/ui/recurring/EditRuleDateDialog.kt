package com.simon.budgetapp.ui.recurring

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simon.budgetapp.network.RecurringRule
import com.simon.budgetapp.ui.components.DatePickerField

@Composable
fun EditRuleDateDialog(
    rule: RecurringRule,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var date by remember { mutableStateOf(rule.next_run_date.take(10)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la date") },
        text = {
            Column {
                DatePickerField(
                    selectedDate = date,
                    onDateSelected = { date = it },
                    label = "Prochaine échéance"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Les transactions déjà créées ne sont pas modifiées.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(date) }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
package com.simon.budgetapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: String, // format "yyyy-MM-dd"
    onDateSelected: (String) -> Unit,
    label: String = "Date"
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val displayFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val isoFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val displayDate = remember(selectedDate) {
        try {
            displayFormat.format(isoFormat.parse(selectedDate)!!)
        } catch (e: Exception) {
            selectedDate
        }
    }

    OutlinedTextField(
        value = displayDate,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Choisir une date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        val initialMillis = try {
            isoFormat.parse(selectedDate)?.time
        } catch (e: Exception) {
            null
        } ?: System.currentTimeMillis()

        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(isoFormat.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
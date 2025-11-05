package com.youth.habittracker.presentation.habits.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    selectedTime: String?,
    onTimeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val currentTime = selectedTime?.let { time ->
        val parts = time.split(":")
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
    } ?: LocalTime.of(9, 0)

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                onTimeSelected("${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}")
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            initialTime = currentTime
        )
    }

    Column(modifier = modifier) {
        Text(
            text = "Reminder Time (Optional)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTime ?: "No reminder set",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedTime != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (selectedTime != null) {
                    TextButton(
                        onClick = { onTimeSelected(null) }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    initialTime: LocalTime
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
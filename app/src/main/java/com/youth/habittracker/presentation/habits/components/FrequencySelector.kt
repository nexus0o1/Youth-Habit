package com.youth.habittracker.presentation.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.youth.habittracker.data.model.Frequency
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@Composable
fun FrequencySelector(
    selectedFrequency: Frequency,
    selectedWeekdays: List<Int>,
    onFrequencySelected: (Frequency) -> Unit,
    onWeekdaysSelected: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Frequency options
        Column(modifier = Modifier.selectableGroup()) {
            Frequency.values().forEach { frequency ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFrequency == frequency,
                        onClick = { onFrequencySelected(frequency) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getFrequencyDisplayName(frequency),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Weekday selector for weekly frequency
        if (selectedFrequency == Frequency.WEEKLY) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select Days:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            WeekdaySelector(
                selectedWeekdays = selectedWeekdays,
                onWeekdaysSelected = onWeekdaysSelected
            )
        }
    }
}

@Composable
private fun WeekdaySelector(
    selectedWeekdays: List<Int>,
    onWeekdaysSelected: (List<Int>) -> Unit
) {
    val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(weekdays.indices) { index ->
            val isSelected = selectedWeekdays.contains(index)

            FilterChip(
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedWeekdays - index
                    } else {
                        selectedWeekdays + index
                    }
                    onWeekdaysSelected(newSelection)
                },
                label = { Text(weekdays[index]) },
                selected = isSelected
            )
        }
    }
}

@Composable
private fun getFrequencyDisplayName(frequency: Frequency): String {
    return when (frequency) {
        Frequency.DAILY -> "Daily"
        Frequency.WEEKLY -> "Weekly (select specific days)"
        Frequency.MONTHLY -> "Monthly"
        Frequency.CUSTOM -> "Custom Schedule"
    }
}
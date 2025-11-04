package com.youth.habittracker.presentation.habits.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youth.habittracker.presentation.theme.*

@Composable
fun HabitCategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Health" to CategoryHealth,
        "Work" to Color(0xFF3b82f6),
        "Personal" to Color(0xFF8b5cf6),
        "Fitness" to Color(0xFFf59e0b),
        "Learning" to Color(0xFFec4899),
        "Creativity" to Color(0xFFf97316),
        "Social" to Color(0xFF06b6d4),
        "Finance" to Color(0xFF84cc16)
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (category, color) ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = selectedCategory == category,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.1f),
                    selectedLabelColor = color,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (selectedCategory == category) {
                        color
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    borderWidth = if (selectedCategory == category) 2.dp else 1.dp
                )
            )
        }
    }
}
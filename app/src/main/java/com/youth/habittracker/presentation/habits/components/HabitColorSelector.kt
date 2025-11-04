package com.youth.habittracker.presentation.habits.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.youth.habittracker.presentation.theme.*

@Composable
fun HabitColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        "#6366f1", // Primary Blue
        "#8b5cf6", // Purple
        "#ec4899", // Pink
        "#f59e0b", // Orange
        "#10b981", // Green
        "#06b6d4", // Cyan
        "#f43f5e", // Rose
        "#64748b", // Gray
        "#f97316", // Orange-red
        "#84cc16"  // Lime
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(colors) { colorHex ->
            val color = Color(android.graphics.Color.parseColor(colorHex))
            val isSelected = selectedColor == colorHex

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 3.dp else 2.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                if (isSelected) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
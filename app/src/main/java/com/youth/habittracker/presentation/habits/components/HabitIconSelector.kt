package com.youth.habittracker.presentation.habits.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HabitIconSelector(
    selectedIcon: String,
    selectedColor: Color,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = listOf(
        "✓", "★", "♥", "☀", "☕", "🏃", "📚", "💧", "🧘", "💪",
        "🎯", "🎨", "🎵", "🌱", "🚀", "💡", "🔥", "⚡", "🌙", "💭"
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(icons) { icon ->
            val isSelected = selectedIcon == icon

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = 0.1f))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) {
                            selectedColor
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(icon) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) {
                        selectedColor
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
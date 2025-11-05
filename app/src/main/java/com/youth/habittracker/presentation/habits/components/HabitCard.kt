package com.youth.habittracker.presentation.habits.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youth.habittracker.data.model.Habit
import com.youth.habittracker.data.model.HabitEntry
import com.youth.habittracker.presentation.theme.CategoryHealth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    todayEntry: HabitEntry?,
    onComplete: (String) -> Unit,
    onSkip: (String) -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isCompleted = todayEntry?.completed == true
    val isSkipped = todayEntry?.completed == false

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Habit info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Habit icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Color(android.graphics.Color.parseColor(habit.color))
                                    .copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = habit.icon,
                            fontSize = 24.sp,
                            color = Color(android.graphics.Color.parseColor(habit.color))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = habit.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (isCompleted) {
                                TextDecoration.LineThrough
                            } else {
                                null
                            }
                        )

                        habit.description?.let { description ->
                            Text(
                                text = description,
                                fontSize = 14.sp,
                                color = if (isCompleted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Category and schedule info
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.padding(end = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = getCategoryColor(habit.category).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = habit.category,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = getCategoryColor(habit.category),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (habit.schedule.timeOfDay != null) {
                                Text(
                                    text = habit.schedule.timeOfDay!!,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Menu button
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = if (isCompleted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit(habit)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                // TODO: Implement archive
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                onDelete(habit.habitId)
                                showMenu = false
                            }
                        )
                    }
                }
            }

            // Action buttons
            if (!isCompleted && !isSkipped) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSkip(habit.habitId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Skip")
                    }

                    Button(
                        onClick = { onComplete(habit.habitId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete")
                    }
                }
            } else if (isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Completed!",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (isSkipped) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Skipped",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "health" -> CategoryHealth
        "work" -> Color(0xFF3b82f6)
        "personal" -> Color(0xFF8b5cf6)
        "fitness" -> Color(0xFFf59e0b)
        "learning" -> Color(0xFFec4899)
        "creativity" -> Color(0xFFf97316)
        "social" -> Color(0xFF06b6d4)
        "finance" -> Color(0xFF84cc16)
        else -> Color(0xFF6366f1)
    }
}
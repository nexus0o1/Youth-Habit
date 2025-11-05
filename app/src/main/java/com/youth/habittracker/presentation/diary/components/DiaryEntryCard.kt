package com.youth.habittracker.presentation.diary.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youth.habittracker.data.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryCard(
    entry: DiariaryEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                // Left side: Date and mood
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mood indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                getMoodColor(entry.mood.primary).copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.mood.primary,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = formatDate(entry.createdAt?.toDate()),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        entry.title?.let { title ->
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = formatTime(entry.createdAt?.toDate()),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Menu button
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content preview
            entry.content.let { content ->
                Text(
                    text = content,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tags
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    if (entry.tags.size > 3) {
                        Text(
                            text = "+${entry.tags.size - 3}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Attachments indicator
            if (entry.photos.isNotEmpty() || entry.voiceMemoUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (entry.photos.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${entry.photos.size} photo${if (entry.photos.size > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (entry.photos.isNotEmpty() && entry.voiceMemoUrl != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (entry.voiceMemoUrl != null) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Voice memo",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Privacy indicator
            if (entry.isPrivate) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Private entry",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getMoodColor(mood: String): Color {
    return when (mood) {
        "😊", "😃", "🙂" -> Color(0xFFfbbf24) // Happy yellows
        "😤", "😠", "👿" -> Color(0xFFef4444) // Angry reds
        "😢", "😭", "💔" -> Color(0xFF64748b) // Sad grays
        "😴", "😪", "😵" -> Color(0xFF6b7280) // Tired grays
        "😰", "😨", "😱" -> Color(0xFFa78bfa) // Anxious purples
        "🧘", "😌", "😇" -> Color(0xFF10b981) // Calm greens
        "🙏", "🥰", "💕" -> Color(0xFFec4899) // Grateful pinks
        else -> Color(0xFF6366f1) // Default blue
    }
}

private fun formatDate(date: Date?): String {
    if (date == null) return ""
    val calendar = Calendar.getInstance()
    calendar.time = date
    val today = Calendar.getInstance()

    return when {
        isSameDay(calendar, today) -> "Today"
        isYesterday(calendar, today) -> "Yesterday"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}

private fun formatTime(date: Date?): String {
    if (date == null) return ""
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(cal1, yesterday)
}
package com.youth.habittracker.presentation.diary

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youth.habittracker.data.model.User
import com.youth.habittracker.presentation.common.UiState
import com.youth.habittracker.presentation.diary.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    currentUser: User,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val createEntryState by viewModel.createEntryState.collectAsState()

    var showCreateEntry by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Handle create entry state
    LaunchedEffect(createEntryState) {
        if (createEntryState is UiState.Success) {
            showCreateEntry = false
            viewModel.clearCreateEntryState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Diary",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateEntry = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Entry"
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (diaryEntries.isEmpty()) {
                EmptyDiaryState(
                    onCreateEntry = { showCreateEntry = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Search bar (animated)
                    AnimatedVisibility(
                        visible = showSearch,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                viewModel.searchEntries(it)
                            },
                            onSearch = { /* Handle search submission */ },
                            onActiveChange = { showSearch = it },
                            active = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Search suggestions could go here
                        }
                    }

                    // Diary entries list or search results
                    val entriesToShow = when (searchState) {
                        is UiState.Success -> searchState.data
                        is UiState.Loading -> diaryEntries // Show original while loading
                        is UiState.Error -> diaryEntries // Show original on error
                        else -> diaryEntries
                    }

                    DiaryEntriesList(
                        entries = entriesToShow,
                        isLoading = searchState is UiState.Loading,
                        onEntryClick = { entry ->
                            // Navigate to entry details
                        },
                        onEntryEdit = { entry ->
                            // Navigate to edit screen
                        },
                        onEntryDelete = { entryId ->
                            viewModel.deleteDiaryEntry(entryId)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Create entry dialog/screen
    if (showCreateEntry) {
        DiaryEntryFormDialog(
            onDismiss = { showCreateEntry = false },
            onSaveComplete = {
                showCreateEntry = false
            }
        )
    }
}

@Composable
private fun EmptyDiaryState(
    onCreateEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Start Your Journal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Document your thoughts, track your mood, and reflect on your day",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateEntry
        ) {
            Text("Write Your First Entry")
        }
    }
}

@Composable
private fun DiaryEntriesList(
    entries: List<DiaryEntry>,
    isLoading: Boolean,
    onEntryClick: (DiaryEntry) -> Unit,
    onEntryEdit: (DiaryEntry) -> Unit,
    onEntryDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = entries,
            key = { entry -> entry.entryId }
        ) { entry ->
            DiaryEntryCard(
                entry = entry,
                onClick = { onEntryClick(entry) },
                onEdit = { onEntryEdit(entry) },
                onDelete = { onEntryDelete(entry.entryId) },
                modifier = Modifier.animateItemPlacement()
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun DiaryEntryFormDialog(
    onDismiss: () -> Unit,
    onSaveComplete: () -> Unit
) {
    // Placeholder for the diary entry form
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Diary Entry") },
        text = {
            Text("Diary entry form would appear here with rich text editing, mood tracking, photo upload, etc.")
        },
        confirmButton = {
            TextButton(onClick = onSaveComplete) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
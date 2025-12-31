package com.birthdaytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.viewmodel.BirthdayViewModel
import com.birthdaytracker.viewmodel.SortOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ListViewScreen(
    viewModel: BirthdayViewModel = hiltViewModel(),
    onAddClick: () -> Unit,
    onBirthdayClick: (Birthday) -> Unit
) {
    val birthdays by viewModel.birthdays.collectAsState(initial = emptyList())
    val sortOption by viewModel.sortOption.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    // Filter birthdays by search query
    val filteredBirthdays = remember(birthdays, searchQuery) {
        if (searchQuery.isBlank()) {
            birthdays
        } else {
            birthdays.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Sort filtered birthdays
    val sortedBirthdays = remember(filteredBirthdays, sortOption, sortAscending) {
        val sorted = when (sortOption) {
            SortOption.DATE -> filteredBirthdays.sortedWith(compareBy { birthday ->
                val today = LocalDate.now()
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear
                upcoming.toEpochDay()
            })
            SortOption.NAME -> filteredBirthdays.sortedBy { it.name }
            SortOption.CATEGORY -> filteredBirthdays.sortedBy { it.category }
        }
        if (sortAscending) sorted else sorted.reversed()
    }

    val nextUpcoming = viewModel.getNextUpcomingBirthday(sortedBirthdays)
    val todayBirthday = sortedBirthdays.firstOrNull { viewModel.isToday(it) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Birthday")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by name...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Search, contentDescription = "Clear search")
                            }
                        }
                    }
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { showSearchBar = !showSearchBar }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                }
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(
                        if (sortAscending) Icons.Filled.ArrowUpward
                        else Icons.Filled.ArrowDownward,
                        contentDescription = if (sortAscending) "Ascending" else "Descending"
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Name",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Text(
                            "Date",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Text(
                            "Category",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                items(
                    items = sortedBirthdays,
                    key = { it.id }
                ) { birthday ->
                    val backgroundColor = when {
                        todayBirthday?.id == birthday.id -> Color(0xFF4CAF50)
                        nextUpcoming?.id == birthday.id -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    BirthdayRow(
                        birthday = birthday,
                        backgroundColor = backgroundColor,
                        onClick = { onBirthdayClick(birthday) }
                    )
                }
            }
        }
    }

    if (showSortMenu) {
        AlertDialog(
            onDismissRequest = { showSortMenu = false },
            title = { Text("Sort By") },
            text = {
                Column {
                    SortOption.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setSortOption(option) }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = sortOption == option,
                                onClick = { viewModel.setSortOption(option) }
                            )
                            Text(
                                text = when (option) {
                                    SortOption.DATE -> "Date"
                                    SortOption.NAME -> "Name"
                                    SortOption.CATEGORY -> "Category"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortMenu = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun BirthdayRow(
    birthday: Birthday,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val today = LocalDate.now()

    // Calculate current age (how old they are TODAY)
    val currentAge = if (birthday.birthDate.isAfter(today)) {
        0
    } else {
        java.time.Period.between(birthday.birthDate, today).years
    }

    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = birthday.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text(
                    text = birthday.birthDate.format(formatter),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Age $currentAge",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = birthday.category.ifEmpty { "-" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
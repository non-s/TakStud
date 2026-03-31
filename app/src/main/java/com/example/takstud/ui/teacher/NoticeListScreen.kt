@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.takstud.R
import com.example.takstud.model.Notice
import com.example.takstud.ui.components.DeletableInfoCard
import com.example.takstud.ui.components.SearchAndFilterPanel
import com.example.takstud.ui.components.SortOption

@Composable
fun NoticeListScreen(
    modifier: Modifier = Modifier,
    notices: List<Notice>,
    onAddNotice: () -> Unit,
    onNoticeClick: (Notice) -> Unit,
    onDeleteNotice: (Notice) -> Unit,
    onBack: () -> Unit
) {
    var noticeToDelete by remember { mutableStateOf<Notice?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(SortOption.DATE_NEWEST) }
    var selectedFilters by remember { mutableStateOf<List<String>>(emptyList()) }

    // Filter and sort notices
    val filteredAndSortedNotices = remember(notices, searchQuery, selectedSort, selectedFilters) {
        var result = notices.filter { notice ->
            notice.title.contains(searchQuery, ignoreCase = true) ||
            notice.studentClass.contains(searchQuery, ignoreCase = true)
        }

        // Apply filters by class
        if (selectedFilters.isNotEmpty()) {
            result = result.filter { it.studentClass in selectedFilters }
        }

        // Apply sorting
        result = when (selectedSort) {
            SortOption.NAME_ASC -> result.sortedBy { it.title }
            SortOption.NAME_DESC -> result.sortedByDescending { it.title }
            SortOption.DATE_NEWEST -> result.sortedByDescending { it.title }
            SortOption.DATE_OLDEST -> result.sortedBy { it.title }
            else -> result
        }

        result
    }

    // Get unique classes for filter options
    val availableFilters = remember(notices) {
        notices.map { it.studentClass }.distinct()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notices)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back)) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNotice) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_notice))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search and Filter Panel
            SearchAndFilterPanel(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                filters = availableFilters,
                selectedFilters = selectedFilters,
                onFilterToggle = { filter ->
                    selectedFilters = if (filter in selectedFilters) {
                        selectedFilters - filter
                    } else {
                        selectedFilters + filter
                    }
                },
                placeholder = "Buscar por título, conteúdo ou turma...",
                showFilters = availableFilters.isNotEmpty()
            )

            if (filteredAndSortedNotices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_notices_registered))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAndSortedNotices, key = { it.id }) { notice ->
                        DeletableInfoCard(
                            title = notice.title,
                            content = stringResource(R.string.class_label, notice.studentClass),
                            onClick = { onNoticeClick(notice) },
                            onDelete = { noticeToDelete = notice }
                        )
                    }
                }
            }
        }
    }

    noticeToDelete?.let {
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            title = { Text(stringResource(R.string.delete_notice_title)) },
            text = { Text(stringResource(R.string.delete_notice_message, it.title)) },
            confirmButton = { Button(onClick = { onDeleteNotice(it); noticeToDelete = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { Button(onClick = { noticeToDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

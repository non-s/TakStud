@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.takstud.R
import com.example.takstud.model.Period
import com.example.takstud.model.Schedule
import com.example.takstud.ui.components.DeletableInfoCard

@Composable
fun SchedulesListScreen(
    modifier: Modifier = Modifier,
    schedules: List<Schedule>,
    onAddSchedule: () -> Unit,
    onScheduleClick: (Schedule) -> Unit,
    onDeleteSchedule: (Schedule) -> Unit,
    onAddMissingSchedules: () -> Unit,
    onBack: () -> Unit
) {
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = Period.values().map { it.name }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.class_schedules)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back)) } },
                actions = {
                    IconButton(onClick = onAddMissingSchedules) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.add_missing_schedules))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSchedule) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new_schedule))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }

            val filteredSchedules = schedules.filter { it.periodo == Period.values()[tabIndex] }

            Column(Modifier.fillMaxSize().padding(16.dp)) {
                if (filteredSchedules.isEmpty()) {
                    Text(stringResource(R.string.no_schedules_registered))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredSchedules, key = { it.id }) { schedule ->
                            DeletableInfoCard(
                                title = schedule.studentClass,
                                content = stringResource(R.string.click_to_see_or_edit),
                                onClick = { onScheduleClick(schedule) },
                                onDelete = { scheduleToDelete = schedule }
                            )
                        }
                    }
                }
            }
        }
    }

    scheduleToDelete?.let {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text(stringResource(R.string.delete_schedule_title)) },
            text = { Text(stringResource(R.string.delete_schedule_message, it.studentClass)) },
            confirmButton = { Button(onClick = { onDeleteSchedule(it); scheduleToDelete = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { Button(onClick = { scheduleToDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

package com.example.takstud.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takstud.model.Notice
import com.example.takstud.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentNoticeListScreen(
    modifier: Modifier = Modifier,
    notices: List<Notice>,
    onBack: () -> Unit
) {
    var showNoticeDialog by remember { mutableStateOf<Notice?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avisos e Reuniões") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AccentTeal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AccentTeal.copy(alpha = 0.03f),
                                Neutral50
                            )
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (notices.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Filled.Notifications,
                                message = "Nenhum aviso ou reunião encontrado."
                            )
                        }
                    } else {
                        items(notices) { notice ->
                            ParentNoticeCardPremium(notice) {
                                showNoticeDialog = notice
                            }
                        }
                    }
                }
            }
        }
    )

    // Notice Dialog
    showNoticeDialog?.let { notice ->
        AlertDialog(
            onDismissRequest = { showNoticeDialog = null },
            icon = {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text(notice.title, fontWeight = FontWeight.Bold) },
            text = { Text(notice.description) },
            confirmButton = {
                Button(
                    onClick = { showNoticeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
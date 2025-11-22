package com.example.takstud.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.takstud.model.chat.ChatChannel
import com.example.takstud.model.chat.ChatMessage
import com.example.takstud.ui.common.UiState
import com.example.takstud.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    currentUserId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val channelsState by viewModel.channels.collectAsState()

    LaunchedEffect(currentUserId) {
        viewModel.loadChannels(currentUserId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mensagens") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = channelsState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    LazyColumn {
                        items(state.data) { channel ->
                            ChatChannelItem(
                                channel = channel,
                                currentUserId = currentUserId,
                                onClick = { navController.navigate("chat_detail/${channel.id}") }
                            )
                            Divider()
                        }
                    }
                }
                is UiState.Error -> Text("Erro: ${state.message}", modifier = Modifier.align(Alignment.Center))
                else -> {}
            }
        }
    }
}

@Composable
fun ChatChannelItem(
    channel: ChatChannel,
    currentUserId: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(channel.getDisplayName(currentUserId), fontWeight = FontWeight.Bold) },
        supportingContent = {
            Text(
                text = channel.lastMessage?.content ?: "Sem mensagens",
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTime(channel.updatedAt),
                    style = MaterialTheme.typography.labelSmall
                )
                // TODO: Badge for unread count
            }
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = channel.getDisplayName(currentUserId).take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    channelId: String,
    currentUserId: String,
    currentUserName: String, // Passado via nav args ou obtido do user session
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(channelId) {
        viewModel.loadMessages(channelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") }, // TODO: Mostrar nome do canal
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    placeholder = { Text("Digite uma mensagem...") },
                    shape = RoundedCornerShape(24.dp)
                )
                IconButton(onClick = {
                    viewModel.sendMessage(messageText, currentUserId, currentUserName)
                    messageText = ""
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = messagesState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        reverseLayout = false // Firestore order is ASC, so we scroll to bottom usually. 
                        // Or use reverseLayout=true and DESC order. For now standard list.
                    ) {
                        items(state.data) { message ->
                            ChatMessageItem(message = message, isMe = message.senderId == currentUserId)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is UiState.Error -> Text("Erro: ${state.message}", modifier = Modifier.align(Alignment.Center))
                else -> {}
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

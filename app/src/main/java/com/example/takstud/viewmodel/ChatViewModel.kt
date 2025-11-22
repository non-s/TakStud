package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.ChatRepository
import com.example.takstud.model.chat.ChatChannel
import com.example.takstud.model.chat.ChatMessage
import com.example.takstud.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _channels = MutableStateFlow<UiState<List<ChatChannel>>>(UiState.Loading())
    val channels: StateFlow<UiState<List<ChatChannel>>> = _channels.asStateFlow()

    private val _messages = MutableStateFlow<UiState<List<ChatMessage>>>(UiState.Loading())
    val messages: StateFlow<UiState<List<ChatMessage>>> = _messages.asStateFlow()

    private val _currentChannelId = MutableStateFlow<String?>(null)

    fun loadChannels(userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.getChannelsForUser(userId).collect { channelList ->
                    _channels.value = UiState.Success(channelList)
                }
            } catch (e: Exception) {
                _channels.value = UiState.Error(e.message ?: "Erro ao carregar chats")
            }
        }
    }

    fun loadMessages(channelId: String) {
        _currentChannelId.value = channelId
        viewModelScope.launch {
            _messages.value = UiState.Loading()
            try {
                chatRepository.getMessagesForChannel(channelId).collect { messageList ->
                    _messages.value = UiState.Success(messageList)
                }
            } catch (e: Exception) {
                _messages.value = UiState.Error(e.message ?: "Erro ao carregar mensagens")
            }
        }
    }

    fun sendMessage(content: String, senderId: String, senderName: String) {
        val channelId = _currentChannelId.value ?: return
        if (content.isBlank()) return

        viewModelScope.launch {
            val message = ChatMessage(
                channelId = channelId,
                senderId = senderId,
                senderName = senderName,
                content = content
            )
            try {
                chatRepository.sendMessage(channelId, message)
            } catch (e: Exception) {
                // Handle error (e.g., show toast)
            }
        }
    }
    
    fun createChannel(channel: ChatChannel, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val id = chatRepository.createChannel(channel)
                onSuccess(id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

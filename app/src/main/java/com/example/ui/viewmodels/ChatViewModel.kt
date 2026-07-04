package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Message
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID



class ChatViewModel constructor(
    private val repository: AppRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActiveMessages(workspaceId: Flow<String?>): StateFlow<List<Message>> {
        return workspaceId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesForWorkspace(id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    fun sendMessage(workspaceId: String, text: String, userId: String, user: UserProfile?) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val msg = Message(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                senderId = userId,
                senderName = user?.displayName ?: "Creator",
                messageBody = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)
        }
    }

    fun sendWorkspaceMessage(workspaceId: String, body: String, user: UserProfile?, attachmentJson: String? = null) {
        viewModelScope.launch {
            val msg = Message(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                senderId = user?.id ?: "me",
                senderName = user?.displayName ?: "Creator",
                messageBody = body,
                attachmentJsonMeta = attachmentJson,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)
        }
    }
}

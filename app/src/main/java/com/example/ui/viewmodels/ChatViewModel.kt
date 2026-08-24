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
    val repository: AppRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActiveMessages(workspaceId: String): Flow<List<Message>> {
        return repository.getMessagesForWorkspace(workspaceId)
    }

    fun getActiveMessagesFromFlow(workspaceId: Flow<String?>): StateFlow<List<Message>> {
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
                senderRole = user?.systemRole ?: "APP_USER",
                messageBody = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)
        }
    }

    fun getDMsForWorkspace(workspaceId: String, userId: String): Flow<List<Message>> {
        return repository.getDMsForWorkspace(workspaceId, userId)
    }

    fun sendDirectMessage(workspaceId: String, recipientId: String, text: String, senderId: String, user: UserProfile?) {
        if (text.isBlank()) return
        val targetWsId = if (workspaceId.isBlank() || workspaceId == "dm_general") {
            "dm_${listOf(senderId, recipientId).sorted().joinToString("_")}"
        } else workspaceId

        viewModelScope.launch {
            val msg = Message(
                id = UUID.randomUUID().toString(),
                workspaceId = targetWsId,
                senderId = senderId,
                recipientId = recipientId,
                senderName = user?.displayName ?: "You",
                senderRole = user?.systemRole ?: "APP_USER",
                messageBody = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)
        }
    }

    fun insertSystemMessage(message: Message) {
        viewModelScope.launch {
            repository.insertMessage(message)
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

    // Direct Messages (Clean MVVM separation additions)
    val creatorsFlow: Flow<List<UserProfile>> = repository.getAllUsersFlow()

    fun getDMsForUser(userId: String): Flow<List<Message>> {
        return repository.getAllDMsForUser(userId)
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun insertMessage(message: Message) {
        viewModelScope.launch {
            repository.insertMessage(message)
        }
    }
}

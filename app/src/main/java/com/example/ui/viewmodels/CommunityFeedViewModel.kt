package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID


class CommunityFeedViewModel constructor(
    private val repository: AppRepository
) : ViewModel() {

    val currentFeedTab = MutableStateFlow("TRENDING")
    val selectedSpaceName = MutableStateFlow("All")

    val trendingPostsList: StateFlow<List<Post>> = repository.trendingPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun resetToast() { _toastMessage.value = null }

    @OptIn(ExperimentalCoroutinesApi::class)
    val postsList: Flow<PagingData<Post>> = combine(currentFeedTab, selectedSpaceName) { tab, space ->
        tab to space
    }.flatMapLatest { (tab, space) ->
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                when (tab) {
                    "NEW" -> repository.getNewPosts()
                    "TOP" -> repository.getTopPosts()
                    else -> repository.getTrendingPosts()
                }
            }
        ).flow.map { pagingData ->
            if (space == "All") pagingData else pagingData.filter { it.spaceName == space }
        }
    }.cachedIn(viewModelScope)

    fun submitPost(title: String, body: String, spaceName: String, user: UserProfile?, onComplete: (() -> Unit)? = null) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        viewModelScope.launch {
            try {
                val post = Post(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    body = body,
                    spaceName = spaceName,
                    authorName = user?.displayName ?: "Creator",
                    authorRole = user?.primarySpecialty ?: "Artist",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertPost(post)
                _toastMessage.value = "New post shared to #$spaceName"
                onComplete?.invoke()
            } catch (e: Exception) {
                _toastMessage.value = "Failed to post: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun votePost(postId: String, voteType: String) {
        viewModelScope.launch {
            try {
                repository.updatePostVote(postId, voteType)
            } catch (e: Exception) {
                _toastMessage.value = "Failed to submit vote: ${e.message}"
            }
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> = repository.commentDao.getCommentsForPost(postId)

    fun submitComment(postId: String, text: String, user: UserProfile?, onComplete: (() -> Unit)? = null) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        viewModelScope.launch {
            try {
                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    postId = postId,
                    authorName = user?.displayName ?: "Creator",
                    authorRole = user?.primarySpecialty ?: "Artist",
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertComment(comment)
                _toastMessage.value = "Comment added."
                onComplete?.invoke()
            } catch (e: Exception) {
                _toastMessage.value = "Failed to add comment: ${e.message}"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun reportPost(postId: String, reason: String, reporterUserId: String) {
        viewModelScope.launch {
            val report = com.example.data.model.Report(
                id = UUID.randomUUID().toString(),
                reporterId = reporterUserId,
                targetType = "POST",
                targetId = postId,
                reason = reason,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            repository.submitReport(report)
            _toastMessage.value = "Report submitted for moderation review."
        }
    }

    fun togglePostInteraction(userId: String, postId: String, interactionType: String) {
        viewModelScope.launch {
            try {
                val existing = repository.getLike(userId, postId, interactionType)
                if (existing != null) {
                    repository.deleteLike(userId, postId, interactionType)
                    _toastMessage.value = "Removed $interactionType"
                } else {
                    val like = com.example.data.model.CommunityLikeEntity(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        postId = postId,
                        interactionType = interactionType,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertLike(like)
                    _toastMessage.value = "Success: $interactionType registered"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Failed: ${e.message}"
            }
        }
    }

    fun getLikeCountFlow(postId: String, interactionType: String): Flow<Int> {
        return repository.getLikeCountFlow(postId, interactionType)
    }

    fun hasLikedFlow(userId: String, postId: String, interactionType: String): Flow<Boolean> = flow {
        emit(repository.getLike(userId, postId, interactionType) != null)
    }
}

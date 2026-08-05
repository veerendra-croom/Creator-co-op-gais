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

    fun submitPost(title: String, body: String, spaceName: String, user: UserProfile?) {
        viewModelScope.launch {
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
        }
    }

    fun votePost(postId: String, voteType: String) {
        viewModelScope.launch {
            repository.updatePostVote(postId, voteType)
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> = repository.commentDao.getCommentsForPost(postId)

    fun submitComment(postId: String, text: String, user: UserProfile?) {
        viewModelScope.launch {
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
}

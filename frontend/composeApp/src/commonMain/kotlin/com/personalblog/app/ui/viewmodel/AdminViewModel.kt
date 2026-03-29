package com.personalblog.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalblog.app.data.remote.AdminPostPayload
import com.personalblog.app.data.remote.AdminRemoteDataSource
import com.personalblog.app.data.remote.TagRemoteDataSource
import com.personalblog.app.data.repository.PostRepository
import com.personalblog.app.logging.LoggerFactory
import com.personalblog.shared.dto.PostDto
import com.personalblog.shared.dto.TagDto
import com.personalblog.shared.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminPostDraft(
    val title: String,
    val slug: String,
    val summary: String,
    val content: String,
    val coverImageUrl: String,
    val selectedTagIds: Set<Long>,
    val published: Boolean
)

data class AdminState(
    val posts: List<PostDto> = emptyList(),
    val users: List<UserDto> = emptyList(),
    val tags: List<TagDto> = emptyList(),
    val isLoadingPosts: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

class AdminViewModel(
    private val postDataSource: PostRepository,
    private val tagDataSource: TagRemoteDataSource,
    private val adminDataSource: AdminRemoteDataSource
) : ViewModel() {
    private val logger = LoggerFactory.getLogger("AdminViewModel")
    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadPosts()
        loadTags()
    }

    fun loadPosts() {
        viewModelScope.launch {
            logger.info("loading admin posts", feature = "admin")
            _state.value = _state.value.copy(isLoadingPosts = true, error = null)
            runCatching { postDataSource.getPosts(1, 100).items }
                .onSuccess { posts ->
                    logger.info("admin posts loaded", feature = "admin", extras = mapOf("count" to posts.size.toString()))
                    _state.value = _state.value.copy(posts = posts, isLoadingPosts = false)
                }
                .onFailure { throwable ->
                    logger.error("failed to load admin posts", feature = "admin", throwable = throwable)
                    _state.value = _state.value.copy(isLoadingPosts = false, error = throwable.message)
                }
        }
    }

    fun loadTags() {
        viewModelScope.launch {
            runCatching { tagDataSource.getTags() }
                .onSuccess { tags ->
                    logger.debug("admin tags loaded", feature = "admin", extras = mapOf("count" to tags.size.toString()))
                    _state.value = _state.value.copy(tags = tags)
                }
                .onFailure { throwable ->
                    logger.error("failed to load tags", feature = "admin", throwable = throwable)
                    _state.value = _state.value.copy(error = throwable.message)
                }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            logger.info("loading users", feature = "admin")
            _state.value = _state.value.copy(isLoadingUsers = true, error = null)
            runCatching { adminDataSource.getUsers() }
                .onSuccess { users ->
                    logger.info("users loaded", feature = "admin", extras = mapOf("count" to users.size.toString()))
                    _state.value = _state.value.copy(users = users, isLoadingUsers = false)
                }
                .onFailure { throwable ->
                    logger.error("failed to load users", feature = "admin", throwable = throwable)
                    _state.value = _state.value.copy(isLoadingUsers = false, error = throwable.message)
                }
        }
    }

    fun loadPostById(postId: Long, onLoaded: (PostDto?) -> Unit) {
        viewModelScope.launch {
            runCatching { postDataSource.getPostById(postId) }
                .onSuccess(onLoaded)
                .onFailure {
                    logger.error("failed to load admin post by id", feature = "admin", throwable = it, extras = mapOf("postId" to postId.toString()))
                    _state.value = _state.value.copy(error = it.message)
                    onLoaded(null)
                }
        }
    }

    fun createPost(draft: AdminPostDraft, onSaved: (PostDto) -> Unit) {
        savePost(postId = null, draft = draft, onSaved = onSaved)
    }

    fun updatePost(postId: Long, draft: AdminPostDraft, onSaved: (PostDto) -> Unit) {
        savePost(postId = postId, draft = draft, onSaved = onSaved)
    }

    private fun savePost(postId: Long?, draft: AdminPostDraft, onSaved: (PostDto) -> Unit) {
        if (draft.selectedTagIds.size > 5) {
            logger.warn("admin draft exceeded tag limit", feature = "admin", extras = mapOf("tagCount" to draft.selectedTagIds.size.toString()))
            _state.value = _state.value.copy(error = "Maximum 5 tags allowed")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            val payload = AdminPostPayload(
                title = draft.title,
                slug = draft.slug.ifBlank { slugify(draft.title) },
                summary = draft.summary,
                content = draft.content,
                coverImageUrl = draft.coverImageUrl.ifBlank { null },
                tagIds = draft.selectedTagIds.toList(),
                published = draft.published
            )

            val saveResult = runCatching {
                if (postId == null) {
                    adminDataSource.createPost(payload)
                } else {
                    adminDataSource.updatePost(postId, payload)
                }
            }

            saveResult
                .onSuccess { post ->
                    logger.info("post saved", feature = "admin", extras = mapOf("postId" to post.id.toString(), "mode" to (if (postId == null) "create" else "update")))
                    _state.value = _state.value.copy(isSaving = false)
                    loadPosts()
                    onSaved(post)
                }
                .onFailure { throwable ->
                    logger.error("failed to save post", feature = "admin", throwable = throwable, extras = mapOf("mode" to (if (postId == null) "create" else "update")))
                    _state.value = _state.value.copy(isSaving = false, error = throwable.message)
                }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching { adminDataSource.deletePost(postId) }
                .onSuccess {
                    logger.info("post deleted", feature = "admin", extras = mapOf("postId" to postId.toString()))
                    _state.value = _state.value.copy(isSaving = false)
                    loadPosts()
                }
                .onFailure { throwable ->
                    logger.error("failed to delete post", feature = "admin", throwable = throwable, extras = mapOf("postId" to postId.toString()))
                    _state.value = _state.value.copy(isSaving = false, error = throwable.message)
                }
        }
    }

    fun updateUserRole(userId: Long, role: String) {
        viewModelScope.launch {
            runCatching { adminDataSource.updateUserRole(userId, role) }
                .onSuccess { success ->
                    logger.info("user role update attempted", feature = "admin", extras = mapOf("userId" to userId.toString(), "role" to role, "success" to success.toString()))
                    if (success) loadUsers()
                    else _state.value = _state.value.copy(error = "Failed to update role")
                }
                .onFailure { throwable ->
                    logger.error("failed to update user role", feature = "admin", throwable = throwable, extras = mapOf("userId" to userId.toString(), "role" to role))
                    _state.value = _state.value.copy(error = throwable.message)
                }
        }
    }

    private fun slugify(title: String): String {
        return title
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }
}

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DispatchLogEntity
import com.example.data.local.QueuePostEntity
import com.example.data.model.BlogPost
import com.example.data.model.HeadlineAnalysis
import com.example.data.model.SocialPlatform
import com.example.data.model.UsTimezone
import com.example.data.model.UsTrafficStatusInfo
import com.example.data.repository.ViralRepository
import com.example.util.UsTrafficAdvisor
import com.example.util.ViralHeadlineOptimizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DASHBOARD,
    BLOG_POSTS,
    OPTIMIZER,
    QUEUE_LOGS,
    SETTINGS
}

data class UiState(
    val currentTab: ScreenTab = ScreenTab.DASHBOARD,
    val usTrafficStatus: UsTrafficStatusInfo = UsTrafficAdvisor.getCurrentTrafficStatus(),
    val bloggerUrl: String = "",
    val posts: List<BlogPost> = emptyList(),
    val isLoadingPosts: Boolean = false,
    val isAutoPostEnabled: Boolean = true,
    val webhookUrl: String = "",
    val appendUtm: Boolean = true,
    val selectedTimezone: UsTimezone = UsTimezone.EASTERN,
    val selectedPostForDetail: BlogPost? = null,
    val headlineInput: String = "",
    val headlineAnalysis: HeadlineAnalysis? = null,
    val singleArticleInput: String = "",
    val isScrapingArticle: Boolean = false,
    val selectedPlatformsForPost: Set<SocialPlatform> = SocialPlatform.values().toSet(),
    val isProcessingDispatch: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ViralRepository(application)

    private val _uiState = MutableStateFlow(
        UiState(
            bloggerUrl = repository.getBloggerUrl(),
            isAutoPostEnabled = repository.isAutoPostEnabled(),
            webhookUrl = repository.getWebhookUrl(),
            appendUtm = repository.isAppendUtmEnabled(),
            headlineAnalysis = ViralHeadlineOptimizer.analyze("10 Proven Ways to Explode Your US Website Traffic in 2026")
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val queuedPosts: StateFlow<List<QueuePostEntity>> = repository.queuedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<DispatchLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSuccessCount: StateFlow<Int> = repository.totalSuccessCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalEstimatedReach: StateFlow<Int?> = repository.totalEstimatedReach
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private var clockTickerJob: Job? = null

    init {
        startClockTicker()
        loadBloggerPosts()
    }

    private fun startClockTicker() {
        clockTickerJob?.cancel()
        clockTickerJob = viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(
                    usTrafficStatus = UsTrafficAdvisor.getCurrentTrafficStatus()
                )
                // If auto post is enabled, check pending queue items
                if (_uiState.value.isAutoPostEnabled) {
                    val count = repository.processPendingQueue()
                    if (count > 0) {
                        _snackbarEvent.emit("🚀 Automated Peak Engine dispatched $count queued posts!")
                    }
                }
                delay(15_000) // update every 15s
            }
        }
    }

    fun setTab(tab: ScreenTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setBloggerUrlInput(url: String) {
        _uiState.value = _uiState.value.copy(bloggerUrl = url)
    }

    fun saveBloggerUrl(url: String) {
        repository.setBloggerUrl(url)
        _uiState.value = _uiState.value.copy(bloggerUrl = url)
        loadBloggerPosts(url)
        viewModelScope.launch {
            _snackbarEvent.emit("Saved Blogger URL. Fetching live posts...")
        }
    }

    fun loadBloggerPosts(customUrl: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPosts = true)
            try {
                val fetched = repository.fetchBlogPosts(customUrl)
                _uiState.value = _uiState.value.copy(
                    posts = fetched,
                    isLoadingPosts = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingPosts = false)
                _snackbarEvent.emit("Could not fetch posts: ${e.message}")
            }
        }
    }

    fun setSingleArticleInput(url: String) {
        _uiState.value = _uiState.value.copy(singleArticleInput = url)
    }

    fun scrapeAndAddSingleArticle() {
        val url = _uiState.value.singleArticleInput.trim()
        if (url.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScrapingArticle = true)
            val post = repository.scrapeSinglePost(url)
            _uiState.value = _uiState.value.copy(isScrapingArticle = false)

            if (post != null) {
                val updated = listOf(post) + _uiState.value.posts.filter { it.url != post.url }
                _uiState.value = _uiState.value.copy(
                    posts = updated,
                    singleArticleInput = "",
                    selectedPostForDetail = post
                )
                _snackbarEvent.emit("Article metadata parsed: ${post.title}")
            } else {
                _snackbarEvent.emit("Failed to scrape article. Added as direct link.")
                val fallback = BlogPost(
                    id = "custom_${System.currentTimeMillis()}",
                    title = "Blogger Article Link",
                    url = url,
                    summary = "Custom Blogger article queued for automated US distribution."
                )
                val updated = listOf(fallback) + _uiState.value.posts
                _uiState.value = _uiState.value.copy(
                    posts = updated,
                    singleArticleInput = "",
                    selectedPostForDetail = fallback
                )
            }
        }
    }

    fun toggleAutoPost(enabled: Boolean) {
        repository.setAutoPostEnabled(enabled)
        _uiState.value = _uiState.value.copy(isAutoPostEnabled = enabled)
        viewModelScope.launch {
            _snackbarEvent.emit(if (enabled) "Auto-Poster ON: Dispatches automatically at US Peak Hours" else "Auto-Poster paused")
        }
    }

    fun setWebhookUrl(url: String) {
        repository.setWebhookUrl(url)
        _uiState.value = _uiState.value.copy(webhookUrl = url)
    }

    fun toggleAppendUtm(enabled: Boolean) {
        repository.setAppendUtmEnabled(enabled)
        _uiState.value = _uiState.value.copy(appendUtm = enabled)
    }

    fun selectPostForDetail(post: BlogPost?) {
        _uiState.value = _uiState.value.copy(selectedPostForDetail = post)
        if (post != null) {
            analyzeHeadline(post.title)
        }
    }

    fun setHeadlineInput(text: String) {
        _uiState.value = _uiState.value.copy(headlineInput = text)
        analyzeHeadline(text)
    }

    fun analyzeHeadline(text: String) {
        val analysis = ViralHeadlineOptimizer.analyze(text)
        _uiState.value = _uiState.value.copy(
            headlineInput = text,
            headlineAnalysis = analysis
        )
    }

    fun togglePlatformSelection(platform: SocialPlatform) {
        val current = _uiState.value.selectedPlatformsForPost.toMutableSet()
        if (current.contains(platform)) {
            if (current.size > 1) current.remove(platform) // Keep at least one
        } else {
            current.add(platform)
        }
        _uiState.value = _uiState.value.copy(selectedPlatformsForPost = current)
    }

    fun queuePostForNextPeak(post: BlogPost) {
        viewModelScope.launch {
            val platforms = _uiState.value.selectedPlatformsForPost.toList()
            val id = repository.queuePostForOptimalUsPeak(post, platforms)
            val info = UsTrafficAdvisor.getCurrentTrafficStatus()
            _snackbarEvent.emit("Scheduled for ${info.nextPeakDescription}! Queued with ID #$id")
        }
    }

    fun cancelQueuedPost(id: Long) {
        viewModelScope.launch {
            repository.cancelQueuedPost(id)
            _snackbarEvent.emit("Queued post #$id cancelled.")
        }
    }

    fun dispatchPostToPlatform(post: BlogPost, platform: SocialPlatform, launchIntent: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingDispatch = true)
            val result = repository.dispatchImmediate(post, platform, launchIntent)
            _uiState.value = _uiState.value.copy(isProcessingDispatch = false)
            _snackbarEvent.emit(result.message)
        }
    }

    fun dispatchPostToAllPlatforms(post: BlogPost) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingDispatch = true)
            val platforms = _uiState.value.selectedPlatformsForPost.toList()
            val results = repository.dispatchAllPlatforms(post, platforms)
            _uiState.value = _uiState.value.copy(isProcessingDispatch = false)

            val successful = results.count { it.success }
            _snackbarEvent.emit("🚀 Dispatched to $successful platforms! Total estimated reach: +${results.sumOf { it.estimatedReach }} US impressions")
        }
    }

    fun testWebhook() {
        val webhook = _uiState.value.webhookUrl.trim()
        if (webhook.isEmpty()) {
            viewModelScope.launch {
                _snackbarEvent.emit("Please enter a Webhook URL first (e.g. Zapier, Make, n8n, or custom server).")
            }
            return
        }

        viewModelScope.launch {
            val sample = _uiState.value.posts.firstOrNull() ?: BlogPost(
                id = "test",
                title = "Test Blogger Connection: High US Traffic Viral Push",
                url = "https://viralusblog.blogspot.com/test-article.html",
                summary = "Testing automated webhook delivery for Blogger links."
            )
            val res = repository.dispatchImmediate(sample, SocialPlatform.PINTEREST, launchIntent = false)
            _snackbarEvent.emit(if (res.success) "Webhook Test Succeeded! Payload delivered." else res.message)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _snackbarEvent.emit("Dispatch history cleared.")
        }
    }
}

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.SavedVideoEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.repository.VideoRepository
import com.example.model.AiInsightResult
import com.example.model.CategoryItem
import com.example.model.ChannelItem
import com.example.model.CommentItem
import com.example.model.NewPipeStreamInfo
import com.example.model.PlaylistItem
import com.example.model.VideoItem
import com.example.ui.player.PlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME, SHORTS, SEARCH, SUBSCRIPTIONS, LIBRARY
}

class YouTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoRepository(application)
    val playerController: PlayerController by lazy { PlayerController(application) }

    // Navigation
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Categories
    val categories: List<CategoryItem> = repository.categories
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Home Feed
    private val _homeVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val homeVideos: StateFlow<List<VideoItem>> = _homeVideos.asStateFlow()
    private val _isHomeLoading = MutableStateFlow(false)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()
    private val _isHomeLoadingMore = MutableStateFlow(false)
    val isHomeLoadingMore: StateFlow<Boolean> = _isHomeLoadingMore.asStateFlow()
    private var currentHomeCategoryPage = 1

    // Shorts Feed
    private val _shortsList = MutableStateFlow<List<VideoItem>>(emptyList())
    val shortsList: StateFlow<List<VideoItem>> = _shortsList.asStateFlow()

    private var suggestionJob: Job? = null

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<VideoItem>>(emptyList())
    val searchResults: StateFlow<List<VideoItem>> = _searchResults.asStateFlow()
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    val recentSearches: StateFlow<List<SearchHistoryEntity>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscriptions
    val subscribedChannels: StateFlow<List<SubscriptionEntity>> = repository.getSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History & Library & Downloads
    val watchHistory: StateFlow<List<HistoryEntity>> = repository.getWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val downloads: StateFlow<List<DownloadEntity>> = repository.getDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val likedVideos: StateFlow<List<SavedVideoEntity>> = repository.getSavedVideos("LIKED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val watchLaterVideos: StateFlow<List<SavedVideoEntity>> = repository.getSavedVideos("WATCH_LATER")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val playlists: List<PlaylistItem> = repository.getPlaylists()

    // Active Video Player
    private val _activeVideo = MutableStateFlow<VideoItem?>(null)
    val activeVideo: StateFlow<VideoItem?> = _activeVideo.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    // Player quality is persisted. Auto always selects the highest playable progressive stream.
    private val playerPrefs = application.getSharedPreferences("romitube_player_prefs", 0)
    private val _videoQuality = MutableStateFlow(playerPrefs.getString("video_quality", "Auto") ?: "Auto")
    val videoQuality: StateFlow<String> = _videoQuality.asStateFlow()

    // Download modal sheet
    private val _downloadTargetVideo = MutableStateFlow<VideoItem?>(null)
    val downloadTargetVideo: StateFlow<VideoItem?> = _downloadTargetVideo.asStateFlow()

    // NewPipe Extractor Stream Info
    private val _streamInfo = MutableStateFlow<NewPipeStreamInfo?>(null)
    val streamInfo: StateFlow<NewPipeStreamInfo?> = _streamInfo.asStateFlow()
    private val _isStreamInfoLoading = MutableStateFlow(false)
    val isStreamInfoLoading: StateFlow<Boolean> = _isStreamInfoLoading.asStateFlow()
    private val _streamInfoError = MutableStateFlow<String?>(null)
    val streamInfoError: StateFlow<String?> = _streamInfoError.asStateFlow()

    private val _isNewPipeEnabled = MutableStateFlow(repository.pipedService.isExtractorEnabled())
    val isNewPipeEnabled: StateFlow<Boolean> = _isNewPipeEnabled.asStateFlow()
    val isNewPipeExtractorEnabled: StateFlow<Boolean> = _isNewPipeEnabled.asStateFlow()

    private val _extractorLatency = MutableStateFlow(48L)
    val extractorLatency: StateFlow<Long> = _extractorLatency.asStateFlow()

    // Active Video Comments
    private val _activeComments = MutableStateFlow<List<CommentItem>>(emptyList())
    val activeComments: StateFlow<List<CommentItem>> = _activeComments.asStateFlow()

    // Related Videos
    private val _relatedVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val relatedVideos: StateFlow<List<VideoItem>> = _relatedVideos.asStateFlow()

    // AI Copilot
    private val _aiInsight = MutableStateFlow<AiInsightResult?>(null)
    val aiInsight: StateFlow<AiInsightResult?> = _aiInsight.asStateFlow()
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Settings
    private val _customPipedInstance =
        MutableStateFlow(repository.pipedService.getActiveInstance())
    val customPipedInstance: StateFlow<String> = _customPipedInstance.asStateFlow()
    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    init {
        loadHomeFeed("All")
        loadShorts()
        seedDefaultSubscriptions()
    }

    private fun seedDefaultSubscriptions() {
        viewModelScope.launch {
            val defaults = repository.apiService.getSubscribedChannels()
            defaults.forEach {
                repository.toggleSubscribe(it, true)
            }
        }
    }

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun refreshCurrentTab() {
        when (_currentTab.value) {
            ScreenTab.HOME -> loadHomeFeed(_selectedCategory.value)
            ScreenTab.SHORTS -> loadShorts()
            ScreenTab.SEARCH -> if (_searchQuery.value.isNotBlank()) performSearch(_searchQuery.value)
            ScreenTab.SUBSCRIPTIONS -> {
                viewModelScope.launch {
                    val channels = repository.apiService.getSubscribedChannels()
                    channels.forEach { repository.toggleSubscribe(it, true) }
                }
            }
            ScreenTab.LIBRARY -> {
                // Room-backed library flows update automatically; nothing to fetch.
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        currentHomeCategoryPage = 1
        loadHomeFeed(category)
    }

    fun loadHomeFeed(category: String) {
        viewModelScope.launch {
            _isHomeLoading.value = true
            currentHomeCategoryPage = 1
            try {
                _homeVideos.value = repository.getHomeVideos(category, page = 1)
            } finally {
                _isHomeLoading.value = false
            }
        }
    }

    fun loadMoreHomeVideos() {
        if (_isHomeLoading.value || _isHomeLoadingMore.value) return
        viewModelScope.launch {
            _isHomeLoadingMore.value = true
            try {
                currentHomeCategoryPage++
                val nextBatch = repository.getHomeVideos(_selectedCategory.value, page = currentHomeCategoryPage)
                if (nextBatch.isNotEmpty()) {
                    val currentList = _homeVideos.value
                    _homeVideos.value = (currentList + nextBatch).distinctBy { it.id }
                }
            } catch (e: Exception) {
                // Keep list intact
            } finally {
                _isHomeLoadingMore.value = false
            }
        }
    }

    private fun loadShorts() {
        viewModelScope.launch {
            _shortsList.value = repository.getShorts()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        suggestionJob?.cancel()
        if (query.trim().length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }

        val requestedQuery = query.trim()
        suggestionJob = viewModelScope.launch {
            delay(250)
            val suggestions = repository.getSearchSuggestions(requestedQuery)
            if (_searchQuery.value.trim() == requestedQuery) {
                _searchSuggestions.value = suggestions.distinct().take(8)
            }
        }
    }

    fun clearSuggestions() {
        _searchSuggestions.value = emptyList()
    }

    fun performSearch(query: String) {
        _searchQuery.value = query
        _searchSuggestions.value = emptyList()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = repository.searchVideos(query)
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun playVideo(video: VideoItem) {
        _activeVideo.value = video
        _isPlayerExpanded.value = true
        _aiInsight.value = null
        _streamInfo.value = null
        _streamInfoError.value = null
        _activeComments.value = emptyList()
        _relatedVideos.value = emptyList()

        viewModelScope.launch {
            repository.recordWatch(video)
            _activeComments.value = repository.getComments(video.id)

            _isStreamInfoLoading.value = true
            val streamResult = repository.getStreamInfo(video.id)
            val info = streamResult.getOrNull()
            _streamInfo.value = info
            _streamInfoError.value = if (info == null) {
                streamResult.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                    ?: "Couldn't load this video. Check your connection and try again."
            } else {
                null
            }
            _isStreamInfoLoading.value = false

            var related = info?.relatedStreams.orEmpty().filter { it.id != video.id }
            if (related.isEmpty()) {
                val seed = video.title.split(Regex("\\s+"))
                    .filter { it.length > 2 }
                    .take(6)
                    .joinToString(" ")
                if (seed.isNotBlank()) {
                    related = repository.searchVideos(seed, recordHistory = false).filter { it.id != video.id }.take(20)
                }
            }
            _relatedVideos.value = related.distinctBy { it.id }.take(20)

            analyzeVideo(video)
        }
    }

    fun fetchStreamDetails(videoId: String) {
        viewModelScope.launch {
            _isStreamInfoLoading.value = true
            _streamInfoError.value = null
            try {
                val result = repository.getStreamInfo(videoId)
                if (result.isSuccess) {
                    _streamInfo.value = result.getOrNull()
                } else {
                    _streamInfoError.value = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                        ?: "Couldn't load this video. Check your connection and try again."
                }
            } catch (e: Exception) {
                _streamInfoError.value = e.message?.takeIf { it.isNotBlank() }
                    ?: "Couldn't load this video. Check your connection and try again."
            } finally {
                _isStreamInfoLoading.value = false
            }
        }
    }

    fun toggleNewPipeExtractor(enabled: Boolean) {
        repository.pipedService.setExtractorEnabled(enabled)
        _isNewPipeEnabled.value = enabled
        // Refresh feed with new setting
        loadHomeFeed(_selectedCategory.value)
    }

    fun pingExtractor() = pingNewPipeExtractor()

    fun pingNewPipeExtractor() {
        viewModelScope.launch {
            val (success, latency) = repository.pingNewPipe()
            _extractorLatency.value = if (success) latency else -1L
        }
    }

    fun setCustomExtractorInstance(url: String) {
        repository.pipedService.setCustomInstance(url)
        pingNewPipeExtractor()
    }

    fun minimizePlayer() {
        _isPlayerExpanded.value = false
    }

    fun expandPlayer() {
        _isPlayerExpanded.value = true
    }

    fun closePlayer() {
        playerController.player.stop()
        playerController.player.clearMediaItems()
        _activeVideo.value = null
        _isPlayerExpanded.value = false
        _streamInfo.value = null
        _streamInfoError.value = null
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun setVideoQuality(quality: String) {
        val normalized = quality.trim().ifBlank { "Auto" }
        _videoQuality.value = normalized
        playerPrefs.edit().putString("video_quality", normalized).apply()
    }

    fun promptDownload(video: VideoItem) {
        _downloadTargetVideo.value = video
    }

    fun dismissDownloadPrompt() {
        _downloadTargetVideo.value = null
    }

    fun startDownload(video: VideoItem, quality: String, sizeMb: Double) {
        viewModelScope.launch {
            repository.saveDownload(video, quality, sizeMb)
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            repository.deleteDownload(videoId)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearDownloads()
        }
    }


    fun toggleLikeActiveVideo() {
        val current = _activeVideo.value ?: return
        val newLiked = !current.isLiked
        val updated = current.copy(isLiked = newLiked)
        _activeVideo.value = updated
        viewModelScope.launch {
            repository.toggleLike(updated, newLiked)
        }
    }

    fun toggleWatchLaterActiveVideo() {
        val current = _activeVideo.value ?: return
        val newSaved = !current.isSaved
        val updated = current.copy(isSaved = newSaved)
        _activeVideo.value = updated
        viewModelScope.launch {
            repository.toggleWatchLater(updated, newSaved)
        }
    }

    fun postComment(text: String) {
        if (text.isBlank()) return
        val newComment = CommentItem(
            id = "c_${System.currentTimeMillis()}",
            authorName = "You",
            authorAvatarUrl = "https://picsum.photos/seed/user/100/100",
            text = text.trim(),
            publishedTime = "Just now",
            likesCount = 0,
            isLikedByMe = false
        )
        _activeComments.value = listOf(newComment) + _activeComments.value
    }

    fun analyzeVideo(video: VideoItem, customPrompt: String? = null) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                _aiInsight.value = repository.analyzeVideoWithAi(video, customPrompt)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteSearch(query)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun saveCustomPipedInstance(url: String) {
        if (url.isBlank()) {
            repository.pipedService.clearCustomInstance()
            _customPipedInstance.value = repository.pipedService.getActiveInstance()
        } else {
            repository.pipedService.setCustomInstance(url)
            _customPipedInstance.value = repository.pipedService.getActiveInstance()
        }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }

}

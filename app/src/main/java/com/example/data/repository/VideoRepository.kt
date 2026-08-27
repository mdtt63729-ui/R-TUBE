package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.SavedVideoEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.remote.GeminiAiService
import com.example.data.remote.PipedApiService
import com.example.data.remote.YouTubeApiService
import com.example.model.AiInsightResult
import com.example.model.CategoryItem
import com.example.model.ChannelItem
import com.example.model.CommentItem
import com.example.model.NewPipeStreamInfo
import com.example.model.PlaylistItem
import com.example.model.VideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VideoRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val historyDao = db.historyDao()
    private val savedVideoDao = db.savedVideoDao()
    private val subscriptionDao = db.subscriptionDao()
    private val searchHistoryDao = db.searchHistoryDao()
    private val downloadDao = db.downloadDao()
    
    val apiService = YouTubeApiService(context)
    val pipedService = PipedApiService(context)
    val aiService = GeminiAiService()

    val categories = listOf(
        CategoryItem("all", "All"),
        CategoryItem("trending", "Trending"),
        CategoryItem("music", "Music"),
        CategoryItem("gaming", "Gaming"),
        CategoryItem("tech", "Tech & Coding"),
        CategoryItem("cricket", "Cricket"),
        CategoryItem("movies", "Movies & Trailers"),
        CategoryItem("learning", "Science & Learning"),
        CategoryItem("comedy", "Comedy"),
        CategoryItem("podcasts", "Podcasts"),
        CategoryItem("live", "Live Streams")
    )

    fun getWatchHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    fun getSavedVideos(type: String): Flow<List<SavedVideoEntity>> = savedVideoDao.getSavedVideosByType(type)
    fun getSubscriptions(): Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()
    fun getDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun saveDownload(video: VideoItem, quality: String, sizeMb: Double) {
        downloadDao.insertDownload(
            DownloadEntity(
                videoId = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl,
                duration = video.duration,
                quality = quality,
                fileSizeMb = sizeMb,
                progress = 100,
                isCompleted = true,
                downloadedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteDownload(videoId: String) {
        downloadDao.deleteDownload(videoId)
    }

    suspend fun clearDownloads() {
        downloadDao.clearAllDownloads()
    }

    suspend fun getHomeVideos(category: String, page: Int = 1): List<VideoItem> {
        val normalized = category.trim().lowercase()

        if (normalized == "all" || normalized == "trending") {
            val trending = pipedService.getTrendingVideos("IN")
            if (page <= 1 && trending.isNotEmpty()) return trending

            // Piped's trending endpoint has no continuation. If trending is
            // temporarily unavailable, use the normal search endpoint instead
            // of leaving Home completely empty.
            return pipedService.searchVideos(
                if (page <= 1) "latest" else "trending",
                "all"
            ).ifEmpty { trending }
        }

        val query = when (normalized) {
            "music" -> "music"
            "gaming" -> "gaming"
            "tech", "tech & coding" -> "technology android coding"
            "cricket" -> "cricket"
            "movies", "movies & trailers" -> "movie trailers"
            "learning", "science & learning" -> "science technology"
            "comedy" -> "comedy"
            "podcasts" -> "podcast"
            "live", "live streams" -> "live stream"
            else -> category
        }

        val filter = if (normalized == "live" || normalized == "live streams") {
            "livestreams"
        } else {
            "all"
        }

        return pipedService.searchVideos(query, filter)
    }

    suspend fun getSearchSuggestions(query: String): List<String> {
        return pipedService.getSearchSuggestions(query)
    }

    suspend fun getShorts(): List<VideoItem> {
        return pipedService.getShorts()
    }

    suspend fun searchVideos(
        query: String,
        isShortOnly: Boolean = false,
        recordHistory: Boolean = true
    ): List<VideoItem> {
        if (recordHistory && query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim()))
        }

        if (pipedService.isExtractorEnabled()) {
            val filter = if (isShortOnly) "shorts" else "all"
            val results = pipedService.searchVideos(query, filter)
            if (results.isNotEmpty()) return results
        }

        return emptyList()
    }

    suspend fun getStreamInfo(videoId: String): Result<NewPipeStreamInfo> {
        return pipedService.getStreamInfo(videoId)
    }

    suspend fun pingNewPipe(): Pair<Boolean, Long> {
        return pipedService.pingExtractor()
    }

    suspend fun recordWatch(video: VideoItem) {
        historyDao.insertHistory(
            HistoryEntity(
                videoId = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl,
                duration = video.duration,
                viewCountText = video.viewCountText,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleLike(video: VideoItem, isLiked: Boolean) {
        if (isLiked) {
            savedVideoDao.insertSavedVideo(
                SavedVideoEntity(
                    videoId = video.id,
                    title = video.title,
                    channelTitle = video.channelTitle,
                    thumbnailUrl = video.thumbnailUrl,
                    duration = video.duration,
                    viewCountText = video.viewCountText,
                    type = "LIKED"
                )
            )
        } else {
            savedVideoDao.removeSavedVideo(video.id, "LIKED")
        }
    }

    suspend fun toggleWatchLater(video: VideoItem, isSaved: Boolean) {
        if (isSaved) {
            savedVideoDao.insertSavedVideo(
                SavedVideoEntity(
                    videoId = video.id,
                    title = video.title,
                    channelTitle = video.channelTitle,
                    thumbnailUrl = video.thumbnailUrl,
                    duration = video.duration,
                    viewCountText = video.viewCountText,
                    type = "WATCH_LATER"
                )
            )
        } else {
            savedVideoDao.removeSavedVideo(video.id, "WATCH_LATER")
        }
    }

    suspend fun toggleSubscribe(channel: ChannelItem, subscribe: Boolean) {
        if (subscribe) {
            subscriptionDao.insertSubscription(
                SubscriptionEntity(
                    channelId = channel.id,
                    name = channel.name,
                    avatarUrl = channel.avatarUrl,
                    handle = channel.handle,
                    subscriberCount = channel.subscriberCountText
                )
            )
        } else {
            subscriptionDao.deleteSubscription(channel.id)
        }
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun deleteSearch(query: String) {
        searchHistoryDao.deleteSearch(query)
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }

    suspend fun getComments(videoId: String): List<CommentItem> {
        return pipedService.getComments(videoId)
    }

    suspend fun analyzeVideoWithAi(video: VideoItem, question: String? = null): AiInsightResult {
        return aiService.analyzeVideo(video, question)
    }

    fun getPlaylists(): List<PlaylistItem> {
        return listOf(
            PlaylistItem("pl_1", "Favorite Coding & Dev", 14, "https://i.ytimg.com/vi/3JZ_D3ELwOQ/hqdefault.jpg"),
            PlaylistItem("pl_2", "Ultimate Workout Music", 28, "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg"),
            PlaylistItem("pl_3", "Science & Space Documentaries", 9, "https://i.ytimg.com/vi/OPf0YbXqDm0/hqdefault.jpg"),
            PlaylistItem("pl_4", "Stand-Up Comedy Specials", 19, "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg")
        )
    }
}

package com.example.data.remote

import android.content.Context
import com.example.model.ChannelItem
import com.example.model.CommentItem
import com.example.model.VideoItem

/**
 * Backward-compatible facade for legacy RomiTube callers.
 *
 * Network access is now routed through PipedApiService, matching the
 * LibreTube architecture (LibreTube -> Piped -> NewPipeExtractor).
 */
class YouTubeApiService(context: Context) {
    private val piped = PipedApiService(context)

    fun getCustomApiKey(): String = ""

    fun setCustomApiKey(key: String) {
        // Kept for compatibility with the existing settings state.
        // Piped does not require a YouTube Data API key.
    }

    suspend fun searchVideos(
        query: String,
        isShortOnly: Boolean = false
    ): List<VideoItem> =
        piped.searchVideos(query, if (isShortOnly) "shorts" else "all")

    suspend fun getSearchSuggestions(query: String): List<String> =
        piped.getSearchSuggestions(query)

    suspend fun searchChannel(channelQuery: String): ChannelItem? =
        piped.searchChannel(channelQuery)

    suspend fun getTrendingVideos(region: String = "IN"): List<VideoItem> =
        piped.getTrendingVideos(region)

    suspend fun getComments(videoId: String): List<CommentItem> =
        piped.getComments(videoId)

    /**
     * Legacy callers should use PipedApiService for feeds.
     * Returning an empty list prevents fake/hard-coded content from entering
     * the real feed.
     */
    fun getCuratedCatalog(): List<VideoItem> = emptyList()

    /**
     * Legacy startup code used these fake defaults. Keep the API surface but
     * do not inject fabricated subscriptions into the real app.
     */
    fun getSubscribedChannels(): List<ChannelItem> = emptyList()
}

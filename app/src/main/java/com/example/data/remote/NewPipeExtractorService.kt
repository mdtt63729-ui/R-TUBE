package com.example.data.remote

import android.content.Context
import com.example.model.CommentItem
import com.example.model.NewPipeStreamInfo
import com.example.model.VideoItem

/**
 * Compatibility facade for older RomiTube UI/ViewModel code.
 *
 * The implementation now uses NewPipe Extractor directly, avoiding public Piped-instance dependency.
 */
class NewPipeExtractorService(context: Context) {
    private val delegate = PipedApiService(context)

    fun getActiveInstance(): String = delegate.getActiveInstance()
    fun setCustomInstance(url: String) = delegate.setCustomInstance(url)
    fun isExtractorEnabled(): Boolean = delegate.isExtractorEnabled()
    fun setExtractorEnabled(enabled: Boolean) = delegate.setExtractorEnabled(enabled)

    suspend fun getStreamInfo(videoId: String): Result<NewPipeStreamInfo> =
        delegate.getStreamInfo(videoId)

    suspend fun getTrendingVideos(region: String = "IN"): List<VideoItem> =
        delegate.getTrendingVideos(region)

    suspend fun searchVideos(query: String, filter: String = "all"): List<VideoItem> =
        delegate.searchVideos(query, filter)

    suspend fun getComments(videoId: String): List<CommentItem> =
        delegate.getComments(videoId)

    suspend fun getSearchSuggestions(query: String): List<String> =
        delegate.getSearchSuggestions(query)

    suspend fun getShorts(): List<VideoItem> =
        delegate.getShorts()

    suspend fun pingExtractor(): Pair<Boolean, Long> =
        delegate.pingExtractor()
}

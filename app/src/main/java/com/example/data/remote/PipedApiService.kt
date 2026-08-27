package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.model.AudioStream
import com.example.model.ChannelItem
import com.example.model.CommentItem
import com.example.model.KeyMoment
import com.example.model.NewPipeStreamInfo
import com.example.model.SubtitleItem
import com.example.model.VideoItem
import com.example.model.VideoStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.comments.CommentsInfo
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Compatibility facade kept under the old class name so the existing ROMITUBE
 * repository/UI does not need a large migration.
 *
 * IMPORTANT: this is now DIRECT NewPipe Extractor mode.
 * It does NOT depend on public Piped instances. LibreTube uses Piped as its
 * default proxy backend, but public Piped instances are currently unreliable;
 * NewPipe Extractor can perform the extraction locally in the Android app.
 */
class PipedApiService(private val context: Context) {

    private val enabledPrefs = context.getSharedPreferences("piped_api_prefs", Context.MODE_PRIVATE)

    init {
        ensureInitialized()
    }

    fun isExtractorEnabled(): Boolean = enabledPrefs.getBoolean("piped_api_enabled", true)

    fun setExtractorEnabled(enabled: Boolean) {
        enabledPrefs.edit().putBoolean("piped_api_enabled", enabled).apply()
    }

    /** Kept for settings compatibility. Direct mode does not use an instance. */
    fun getActiveInstance(): String = "NewPipe Extractor • Direct YouTube"

    fun setCustomInstance(@Suppress("UNUSED_PARAMETER") url: String) = Unit
    fun clearCustomInstance() = Unit

    suspend fun searchVideos(query: String, filter: String = "all"): List<VideoItem> =
        withContext(Dispatchers.IO) {
            if (query.isBlank() || !isExtractorEnabled()) return@withContext emptyList()
            runCatching {
                ensureInitialized()
                val service = ServiceList.YouTube
                val filters = when (filter.lowercase()) {
                    "shorts" -> listOf("shorts")
                    "channels" -> listOf("channels")
                    "playlists" -> listOf("playlists")
                    "videos" -> listOf("videos")
                    "livestreams" -> listOf("livestreams")
                    else -> emptyList()
                }
                val handler = service.getSearchQHFactory().fromQuery(query.trim(), filters, null)
                val info = SearchInfo.getInfo(service, handler)
                info.relatedItems.mapNotNull { it.toVideoItem("Search") }.take(40)
            }.onFailure { Log.e(TAG, "NewPipe search failed", it) }
                .getOrDefault(emptyList())
        }

    suspend fun getSearchSuggestions(query: String): List<String> =
        withContext(Dispatchers.IO) {
            if (query.isBlank() || !isExtractorEnabled()) return@withContext emptyList()
            runCatching {
                ensureInitialized()
                ServiceList.YouTube.getSuggestionExtractor().suggestionList(query.trim())
                    .filter { it.isNotBlank() }
                    .take(8)
            }.onFailure { Log.w(TAG, "Suggestion extraction failed", it) }
                .getOrDefault(emptyList())
        }

    /**
     * The old public Piped /trending endpoint is intentionally not used.
     * Instead we use several high-signal cold-start searches and merge the
     * results. This keeps Home functional even when YouTube changes the public
     * Trending page shape.
     */
    suspend fun getTrendingVideos(region: String = "IN"): List<VideoItem> =
        withContext(Dispatchers.IO) {
            if (!isExtractorEnabled()) return@withContext emptyList()
            ensureInitialized()

            val queries = listOf(
                "trending $region",
                "popular videos $region",
                "latest videos",
                "YouTube trending"
            )

            val merged = LinkedHashMap<String, VideoItem>()
            for (query in queries) {
                val result = runCatching { searchVideos(query, "all") }
                    .onFailure { Log.w(TAG, "Home fallback search failed: $query", it) }
                    .getOrDefault(emptyList())
                result.forEach { video ->
                    if (video.id.isNotBlank()) merged.putIfAbsent(video.id, video.copy(category = "Trending"))
                }
                if (merged.size >= 30) break
            }
            merged.values.take(30)
        }

    suspend fun getStreamInfo(videoId: String): Result<NewPipeStreamInfo> =
        withContext(Dispatchers.IO) {
            if (videoId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Video ID is empty"))
            runCatching {
                ensureInitialized()
                val info = StreamInfo.getInfo("https://www.youtube.com/watch?v=$videoId")
                info.toNewPipeStreamInfo(videoId)
            }.onFailure { Log.e(TAG, "NewPipe stream extraction failed for $videoId", it) }
        }

    suspend fun getComments(videoId: String): List<CommentItem> =
        withContext(Dispatchers.IO) {
            if (videoId.isBlank() || !isExtractorEnabled()) return@withContext emptyList()
            runCatching {
                ensureInitialized()
                val info = CommentsInfo.getInfo("https://www.youtube.com/watch?v=$videoId")
                info.relatedItems.mapNotNull { it.toCommentItem() }.take(50)
            }.onFailure { Log.w(TAG, "Comment extraction failed", it) }
                .getOrDefault(emptyList())
        }

    suspend fun searchChannel(channelQuery: String): ChannelItem? =
        withContext(Dispatchers.IO) {
            if (channelQuery.isBlank()) return@withContext null
            runCatching {
                ensureInitialized()
                val service = ServiceList.YouTube
                val handler = service.getSearchQHFactory().fromQuery(
                    channelQuery.trim(), listOf("channels"), null
                )
                val info = SearchInfo.getInfo(service, handler)
                val channel = info.relatedItems.firstOrNull { it is org.schabi.newpipe.extractor.channel.ChannelInfoItem }
                    as? org.schabi.newpipe.extractor.channel.ChannelInfoItem
                    ?: return@runCatching null
                ChannelItem(
                    id = channel.url.substringAfter("/channel/").substringBefore('/'),
                    name = channel.name,
                    handle = channel.url.substringAfterLast('/').let { if (it.startsWith("@")) it else "" },
                    avatarUrl = channel.thumbnails.maxByOrNull { it.height }?.url.orEmpty(),
                    subscriberCountText = formatViews(channel.subscriberCount, "subscribers"),
                    videoCountText = "Videos unavailable",
                    description = ""
                )
            }.getOrNull()
        }

    suspend fun getChannel(channelId: String): ChannelItem? =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureInitialized()
                val info = ChannelInfo.getInfo("https://www.youtube.com/channel/$channelId")
                ChannelItem(
                    id = info.id,
                    name = info.name,
                    handle = info.id,
                    avatarUrl = info.avatars.maxByOrNull { it.height }?.url.orEmpty(),
                    bannerUrl = info.banners.maxByOrNull { it.height }?.url.orEmpty(),
                    subscriberCountText = formatViews(info.subscriberCount, "subscribers"),
                    videoCountText = "Videos unavailable",
                    description = info.description
                )
            }.onFailure { Log.w(TAG, "Channel extraction failed", it) }
                .getOrNull()
        }

    suspend fun getShorts(): List<VideoItem> =
        withContext(Dispatchers.IO) {
            val direct = runCatching { searchVideos("shorts", "shorts") }.getOrDefault(emptyList())
            if (direct.isNotEmpty()) direct
            else searchVideos("#shorts", "all").filter { it.isShort }
        }

    suspend fun pingExtractor(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val ok = runCatching {
            ensureInitialized()
            val handler = ServiceList.YouTube.getSearchQHFactory().fromQuery("test", emptyList(), null)
            SearchInfo.getInfo(ServiceList.YouTube, handler).relatedItems.isNotEmpty()
        }.getOrDefault(false)
        ok to if (ok) System.currentTimeMillis() - start else -1L
    }

    private fun ensureInitialized() {
        if (initialized.get()) return
        synchronized(initialized) {
            if (initialized.get()) return
            NewPipe.init(
                NewPipeDownloader(),
                org.schabi.newpipe.extractor.localization.Localization("IN", "en"),
                org.schabi.newpipe.extractor.localization.ContentCountry("IN")
            )
            initialized.set(true)
        }
    }

    private fun InfoItem.toVideoItem(category: String): VideoItem? {
        val item = this as? StreamInfoItem ?: return null
        val itemUrl: String? = item.url
        val id = extractVideoId(itemUrl.orEmpty())
        if (id.isBlank()) return null
        val thumbnail = item.thumbnails.maxByOrNull { it.height }?.url.orEmpty()
            .ifBlank { "https://i.ytimg.com/vi/$id/hqdefault.jpg" }
        val avatar = item.uploaderAvatars.maxByOrNull { it.height }?.url.orEmpty()
        val channelId = item.uploaderUrl.orEmpty().substringAfter("/channel/").substringBefore('/')
        // NewPipeExtractor's Java getters are not null-annotated and can return null
        // for some videos (shorts/livestreams/etc). Read into nullable locals first so
        // Kotlin doesn't insert a "must not be null" runtime assertion here.
        val itemName: String? = item.name
        val uploaderName: String? = item.uploaderName
        val itemDescription: String? = item.shortDescription
        return VideoItem(
            id = id,
            title = itemName.orEmpty().ifBlank { "Untitled video" },
            channelTitle = uploaderName.orEmpty(),
            channelId = channelId,
            channelAvatarUrl = avatar,
            description = itemDescription.orEmpty(),
            publishedAt = item.textualUploadDate.orEmpty().ifBlank { "Recently" },
            duration = formatDuration(item.duration),
            viewCountText = formatViews(item.viewCount, "views"),
            thumbnailUrl = thumbnail,
            videoUrl = itemUrl.orEmpty(),
            isShort = item.isShortFormContent,
            category = category
        )
    }

    private fun StreamInfo.toNewPipeStreamInfo(videoId: String): NewPipeStreamInfo {
        val videoStreams = (videoStreams + videoOnlyStreams).mapNotNull { stream ->
            val url = stream.content.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val durationMs = stream.itagItem?.approxDurationMs ?: (duration * 1000L)
            val sizeMb = if (stream.bitrate > 0 && durationMs > 0) {
                stream.bitrate * durationMs / (8.0 * 1024.0 * 1024.0)
            } else 0.0
            VideoStream(
                url = url,
                quality = stream.getResolution().ifBlank { "${stream.height}p" },
                format = stream.format?.mimeType ?: stream.format?.toString() ?: "video/mp4",
                codec = stream.codec.orEmpty(),
                bitrate = stream.bitrate.toLong(),
                fps = stream.fps,
                width = stream.width,
                height = stream.height,
                sizeMb = sizeMb,
                isVideoOnly = videoOnlyStreams.contains(stream)
            )
        }.sortedWith(compareBy<VideoStream> { it.isVideoOnly }.thenByDescending { it.height }.thenByDescending { it.bitrate })

        val audio = audioStreams.mapNotNull { stream ->
            val url = stream.content.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val durationMs = stream.itagItem?.approxDurationMs ?: (duration * 1000L)
            val sizeMb = if (stream.bitrate > 0 && durationMs > 0) {
                stream.bitrate * durationMs / (8.0 * 1024.0 * 1024.0)
            } else 0.0
            AudioStream(
                url = url,
                quality = "${stream.averageBitrate} kbps",
                format = stream.format?.mimeType ?: stream.format?.toString() ?: "audio/mp4",
                codec = stream.codec.orEmpty(),
                bitrate = stream.bitrate.toLong(),
                sizeMb = sizeMb
            )
        }.sortedByDescending { it.bitrate }

        val subtitles = subtitles.mapNotNull { sub ->
            val url = sub.content.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            SubtitleItem(
                language = sub.displayLanguageName,
                url = url,
                format = sub.format?.mimeType ?: "text/vtt",
                isAutoGenerated = sub.isAutoGenerated
            )
        }

        val chapters = streamSegments.map {
            KeyMoment(formatDuration(it.startTimeSeconds.toLong()), it.title)
        }

        val related = relatedItems.filterIsInstance<StreamInfoItem>().mapNotNull {
            it.toVideoItem("Related")
        }.take(20)

        // Same platform-type null hazard as in toVideoItem(): NewPipeExtractor's Java
        // getters aren't null-annotated, so read into nullable locals before falling
        // back, instead of assigning straight into this data class's non-null fields.
        val streamTitle: String? = name
        val streamUploaderName: String? = uploaderName
        val streamDescriptionContent: String? = description.content

        return NewPipeStreamInfo(
            videoId = videoId,
            title = streamTitle.orEmpty().ifBlank { "Untitled video" },
            uploaderName = streamUploaderName.orEmpty(),
            uploaderUrl = uploaderUrl.orEmpty(),
            uploaderAvatar = uploaderAvatars.maxByOrNull { it.height }?.url.orEmpty(),
            uploaderSubscriberCount = formatViews(uploaderSubscriberCount, "subscribers"),
            uploaderVerified = isUploaderVerified,
            description = streamDescriptionContent.orEmpty(),
            uploadDate = textualUploadDate.orEmpty().ifBlank { "Recently" },
            durationSeconds = duration,
            viewCount = viewCount,
            likeCount = likeCount,
            videoStreams = videoStreams,
            audioStreams = audio,
            hlsUrl = hlsUrl,
            dashUrl = dashMpdUrl,
            subtitles = subtitles,
            chapters = chapters,
            relatedStreams = related,
            extractorSource = "NewPipe Extractor • Direct YouTube"
        )
    }

    private fun CommentsInfoItem.toCommentItem(): CommentItem? {
        val text = commentText.content.trim()
        if (text.isBlank()) return null
        return CommentItem(
            id = commentId,
            authorName = uploaderName,
            authorAvatarUrl = thumbnails.maxByOrNull { it.height }?.url.orEmpty(),
            text = text,
            publishedTime = textualUploadDate,
            likesCount = likeCount.toInt()
        )
    }

    private fun extractVideoId(url: String): String {
        val value = url.trim()
        return when {
            value.contains("watch?v=") -> value.substringAfter("watch?v=").substringBefore('&')
            value.contains("/shorts/") -> value.substringAfter("/shorts/").substringBefore('?').substringBefore('/')
            value.contains("/embed/") -> value.substringAfter("/embed/").substringBefore('?').substringBefore('/')
            value.matches(Regex("[A-Za-z0-9_-]{11}")) -> value
            else -> value.substringAfterLast('/').substringBefore('?').takeIf { it.length == 11 }.orEmpty()
        }
    }

    private fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "0:00"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "$h:%02d:%02d".format(m, s) else "%d:%02d".format(m, s)
    }

    private fun formatViews(value: Long, suffix: String): String = when {
        value <= 0 -> "— $suffix"
        value >= 1_000_000_000 -> "%.1fB $suffix".format(value / 1_000_000_000.0)
        value >= 1_000_000 -> "%.1fM $suffix".format(value / 1_000_000.0)
        value >= 1_000 -> "%.1fK $suffix".format(value / 1_000.0)
        else -> "$value $suffix"
    }

    companion object {
        private const val TAG = "NewPipeDirect"
        private val initialized = AtomicBoolean(false)
    }
}

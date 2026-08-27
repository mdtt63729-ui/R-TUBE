package com.example.ui.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import com.example.data.remote.NewPipeDownloader
import com.example.model.NewPipeStreamInfo
import com.example.model.VideoStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class PlayerController(context: Context) {
    private val httpFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(NewPipeDownloader.USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15_000)
        .setReadTimeoutMs(30_000)
        .setDefaultRequestProperties(
            mapOf(
                "Referer" to "https://www.youtube.com/",
                "Origin" to "https://www.youtube.com",
                "Accept" to "*/*"
            )
        )

    private val mediaSourceFactory = DefaultMediaSourceFactory(httpFactory)

    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setMediaSourceFactory(mediaSourceFactory)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(15_000, 50_000, 1_000, 2_500)
                .build()
        )
        .build()
        .apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
        }

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var candidates: List<MediaSource> = emptyList()
    private var candidateIndex = 0
    private var activeKey: String? = null
    private var released = false

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _isBuffering.value = state == Player.STATE_BUFFERING ||
                    (state == Player.STATE_IDLE && player.playWhenReady)
                if (state == Player.STATE_READY) {
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                    _isBuffering.value = false
                    _error.value = null
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlayerError(error: PlaybackException) {
                if (candidateIndex + 1 < candidates.size) {
                    candidateIndex++
                    val position = player.currentPosition.coerceAtLeast(0L)
                    player.setMediaSource(candidates[candidateIndex])
                    player.prepare()
                    player.seekTo(position)
                    player.playWhenReady = true
                } else {
                    _isBuffering.value = false
                    _isPlaying.value = false
                    _error.value = error.message?.takeIf { it.isNotBlank() } ?: "Playback unavailable"
                }
            }
        })
    }

    fun prepare(
        videoId: String,
        info: NewPipeStreamInfo,
        selectedQuality: String,
        autoPlay: Boolean = true
    ) {
        if (released) return
        val sources = buildCandidates(videoId, info, selectedQuality)
        if (sources.isEmpty()) {
            _error.value = "No playable stream found"
            _isBuffering.value = false
            return
        }

        val key = videoId + "|" + selectedQuality + "|" +
            (info.dashUrl.orEmpty()) + "|" + (info.hlsUrl.orEmpty()) + "|" +
            (info.videoStreams.firstOrNull { it.url.isNotBlank() }?.url.orEmpty())
        if (activeKey == key && player.mediaItemCount > 0) {
            player.playWhenReady = autoPlay
            return
        }

        val sameVideo = activeKey?.startsWith("$videoId|") == true
        val resume = if (sameVideo) player.currentPosition.coerceAtLeast(0L) else 0L
        activeKey = key
        candidates = sources
        candidateIndex = 0
        _error.value = null
        _isBuffering.value = true
        player.setMediaSource(candidates.first())
        player.prepare()
        if (resume > 0L && player.duration > 0L) player.seekTo(resume)
        player.playWhenReady = autoPlay
    }

    fun retry() {
        if (candidates.isEmpty() || released) return
        _error.value = null
        _isBuffering.value = true
        val position = player.currentPosition.coerceAtLeast(0L)
        player.setMediaSource(candidates[candidateIndex])
        player.prepare()
        if (position > 0L) player.seekTo(position)
        player.playWhenReady = true
    }

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun release() {
        if (!released) {
            released = true
            player.release()
        }
    }

    private fun buildCandidates(
        videoId: String,
        info: NewPipeStreamInfo,
        selectedQuality: String
    ): List<MediaSource> {
        val progressive = info.videoStreams
            .filter { !it.isVideoOnly && it.url.isNotBlank() }
            .sortedWith(compareBy<VideoStream> { qualityDistance(it, selectedQuality) }.thenByDescending { it.height }.thenByDescending { it.bitrate })

        val selected = if (selectedQuality.equals("Auto", true)) null else {
            progressive.firstOrNull { qualityDistance(it, selectedQuality) == 0 }
        }
        val sources = mutableListOf<MediaSource>()

        if (selected != null) {
            sources += mediaSource(selected.url, selected.formatMime(), videoId)
        }

        // Adaptive playback is the preferred Auto path. Media3 receives both
        // audio and video tracks from the DASH manifest and reports the real duration.
        if (!info.dashUrl.isNullOrBlank()) {
            sources += mediaSource(info.dashUrl, "application/dash+xml", videoId)
        }

        if (selected == null && progressive.isNotEmpty()) {
            sources += mediaSource(progressive.first().url, progressive.first().formatMime(), videoId)
        }
        if (!info.hlsUrl.isNullOrBlank()) {
            sources += mediaSource(info.hlsUrl, "application/x-mpegURL", videoId)
        }

        // Last resort: merge an adaptive video-only track with the best audio track.
        if (sources.isEmpty()) {
            val videoOnly = info.videoStreams
                .filter { it.isVideoOnly && it.url.isNotBlank() }
                .sortedWith(compareBy<VideoStream> { qualityDistance(it, selectedQuality) }.thenByDescending { it.height }.thenByDescending { it.bitrate })
                .firstOrNull()
            val audio = info.audioStreams.filter { it.url.isNotBlank() }.maxByOrNull { it.bitrate }
            if (videoOnly != null && audio != null) {
                sources += MergingMediaSource(
                    mediaSource(videoOnly.url, videoOnly.formatMime(), videoId),
                    mediaSource(audio.url, audio.formatMime(), videoId)
                )
            }
        }
        return sources
    }

    private fun qualityDistance(stream: VideoStream, requested: String): Int {
        if (requested.equals("Auto", true)) return 0
        val requestedHeight = Regex("(\\d{3,4})").find(requested)?.groupValues?.get(1)?.toIntOrNull() ?: return 10_000
        return kotlin.math.abs(stream.height - requestedHeight)
    }

    private fun mediaSource(url: String, mime: String?, videoId: String): MediaSource {
        val item = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(videoId).build())
            .apply { if (!mime.isNullOrBlank()) setMimeType(mime) }
            .build()
        return mediaSourceFactory.createMediaSource(item)
    }

    private fun VideoStream.formatMime(): String? = format.substringBefore(';').trim().takeIf { it.contains('/') }
    private fun com.example.model.AudioStream.formatMime(): String? = format.substringBefore(';').trim().takeIf { it.contains('/') }
}

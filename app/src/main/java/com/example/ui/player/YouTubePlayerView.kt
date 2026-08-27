package com.example.ui.player

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.NewPipeStreamInfo
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun YouTubePlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    controller: PlayerController? = null,
    streamInfo: NewPipeStreamInfo? = null,
    streamInfoError: String? = null,
    onRetryStreamInfo: (() -> Unit)? = null,
    autoPlay: Boolean = true,
    isShort: Boolean = false,
    selectedQuality: String = "Auto",
    availableQualities: List<String> = emptyList(),
    onQualitySelected: ((String) -> Unit)? = null,
    onMinimize: (() -> Unit)? = null,
    onFullScreenToggle: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val activeController = remember(controller) { controller ?: PlayerController(context) }
    val ownsController = controller == null
    val isPlaying by activeController.isPlaying.collectAsState()
    val isBuffering by activeController.isBuffering.collectAsState()
    val playerError by activeController.error.collectAsState()
    val fallbackDuration by activeController.durationMs.collectAsState()

    var currentTimeMs by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var showQualityMenu by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var doubleTapFeedback by remember { mutableStateOf<String?>(null) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val latestOnMinimize by rememberUpdatedState(onMinimize)
    val latestTap by rememberUpdatedState {
        lastInteraction = System.currentTimeMillis()
        showControls = !showControls
    }

    LaunchedEffect(videoId, streamInfo, selectedQuality) {
        if (streamInfo != null) {
            activeController.prepare(videoId, streamInfo, selectedQuality, autoPlay)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = activeController.player.currentPosition.coerceAtLeast(0L)
            delay(200)
        }
    }

    LaunchedEffect(showControls, isPlaying, lastInteraction) {
        if (showControls && isPlaying) {
            delay(3000)
            if (System.currentTimeMillis() - lastInteraction >= 2900L) showControls = false
        }
    }

    LaunchedEffect(doubleTapFeedback) {
        if (doubleTapFeedback != null) {
            delay(550)
            doubleTapFeedback = null
        }
    }

    DisposableEffect(activeController) {
        onDispose { if (ownsController) activeController.release() }
    }

    val durationMs = maxOf(fallbackDuration, streamInfo?.durationSeconds?.times(1000L) ?: 0L, activeController.player.duration.coerceAtLeast(0L))
    val durationSec = (durationMs / 1000L).coerceAtLeast(1L)
    val currentSec = (currentTimeMs / 1000L).coerceIn(0L, durationSec)

    Box(
        modifier = modifier.background(Color.Black).testTag("youtube_native_player_box"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    resizeMode = if (isShort) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    player = activeController.player

                    val detector = GestureDetector(viewContext, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onDown(e: MotionEvent): Boolean = true
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            latestTap()
                            return true
                        }
                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            lastInteraction = System.currentTimeMillis()
                            val x = e.x
                            if (x < width * .4f) {
                                activeController.player.seekTo((activeController.player.currentPosition - 10_000L).coerceAtLeast(0L))
                                doubleTapFeedback = "−10"
                            } else if (x > width * .6f) {
                                activeController.player.seekTo((activeController.player.currentPosition + 10_000L).coerceAtMost(durationMs))
                                doubleTapFeedback = "+10"
                            } else {
                                activeController.player.playWhenReady = !activeController.player.isPlaying
                            }
                            return true
                        }
                        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                            if (e1 != null && e2.y - e1.y > 140f && kotlin.math.abs(velocityY) > 500f) {
                                latestOnMinimize?.invoke()
                                return true
                            }
                            return false
                        }
                    })
                    setOnTouchListener { _, event ->
                        detector.onTouchEvent(event)
                        true
                    }
                }
            },
            update = { view ->
                if (view.player !== activeController.player) view.player = activeController.player
                view.resizeMode = if (isShort) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        )

        if (streamInfo == null && streamInfoError != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Video couldn't play",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = streamInfoError,
                    color = Color.White.copy(alpha = .7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 24.dp, end = 24.dp)
                )
                IconButton(onClick = { onRetryStreamInfo?.invoke() }, modifier = Modifier.testTag("player_retry_button")) {
                    Icon(Icons.Default.PlayArrow, "Retry", tint = YouTubeRed)
                }
            }
        } else if (streamInfo == null) {
            CircularProgressIndicator(color = YouTubeRed, strokeWidth = 3.dp, modifier = Modifier.size(42.dp))
        } else if (isBuffering) {
            Surface(color = Color.Black.copy(alpha = .45f), shape = CircleShape, modifier = Modifier.size(68.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YouTubeRed, strokeWidth = 3.dp, modifier = Modifier.size(38.dp))
                }
            }
        }

        if (playerError != null && !isBuffering) {
            Surface(color = Color.Black.copy(alpha = .78f), shape = RoundedCornerShape(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text("Video couldn't play", color = Color.White, fontSize = 13.sp)
                    IconButton(onClick = { activeController.retry() }) {
                        Icon(Icons.Default.PlayArrow, "Retry", tint = YouTubeRed)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = doubleTapFeedback != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(color = Color.Black.copy(alpha = .7f), shape = CircleShape, modifier = Modifier.size(82.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(doubleTapFeedback.orEmpty(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }

        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(100.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(.86f), Color.Transparent))))
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { lastInteraction = System.currentTimeMillis(); onMinimize?.invoke() }, modifier = Modifier.size(44.dp).testTag("player_minimize_button")) {
                        Icon(Icons.Default.KeyboardArrowDown, "Minimize", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color.White.copy(.13f), shape = RoundedCornerShape(8.dp)) {
                            Text(selectedQuality.ifBlank { "Auto" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                        }
                        IconButton(onClick = { lastInteraction = System.currentTimeMillis() }) {
                            Icon(Icons.Default.ClosedCaption, "Captions", tint = Color.White)
                        }
                        Box {
                            IconButton(onClick = { lastInteraction = System.currentTimeMillis(); showQualityMenu = true }, modifier = Modifier.testTag("player_settings_button")) {
                                Icon(Icons.Default.Settings, "Playback settings", tint = Color.White)
                            }
                            DropdownMenu(expanded = showQualityMenu, onDismissRequest = { showQualityMenu = false }) {
                                Text("Quality", color = Color.White.copy(.65f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp))
                                (listOf("Auto") + availableQualities).distinct().forEach { quality ->
                                    DropdownMenuItem(
                                        text = { Text(quality, color = Color.White, fontWeight = if (quality.equals(selectedQuality, true)) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = { showQualityMenu = false; onQualitySelected?.invoke(quality) }
                                    )
                                }
                                Text("Playback speed", color = Color.White.copy(.55f), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
                                listOf(.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = { Text(if (speed == 1f) "Normal (1x)" else "${speed}x", color = Color.White, fontWeight = if (speed == playbackSpeed) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = { playbackSpeed = speed; activeController.setSpeed(speed); showQualityMenu = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(Modifier.align(Alignment.Center).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { lastInteraction = System.currentTimeMillis(); activeController.player.seekTo((activeController.player.currentPosition - 10_000L).coerceAtLeast(0L)) }, modifier = Modifier.size(58.dp)) {
                        Icon(Icons.Default.Replay10, "Rewind 10 seconds", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Surface(color = Color.Black.copy(.48f), shape = CircleShape, modifier = Modifier.size(78.dp)) {
                        IconButton(onClick = { lastInteraction = System.currentTimeMillis(); activeController.player.playWhenReady = !activeController.player.isPlaying }, modifier = Modifier.fillMaxSize()) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play or pause", tint = Color.White, modifier = Modifier.size(42.dp))
                        }
                    }
                    IconButton(onClick = { lastInteraction = System.currentTimeMillis(); activeController.player.seekTo((activeController.player.currentPosition + 10_000L).coerceAtMost(durationMs)) }, modifier = Modifier.size(58.dp)) {
                        Icon(Icons.Default.Forward10, "Forward 10 seconds", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                Box(Modifier.fillMaxWidth().height(124.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.92f)))) )
                Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatTime(currentSec), color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = currentSec.toFloat(),
                            onValueChange = { activeController.player.seekTo((it * 1000f).toLong()); lastInteraction = System.currentTimeMillis() },
                            valueRange = 0f..durationSec.toFloat(),
                            modifier = Modifier.weight(1f).height(30.dp),
                            colors = SliderDefaults.colors(thumbColor = YouTubeRed, activeTrackColor = YouTubeRed, inactiveTrackColor = Color.White.copy(.3f))
                        )
                        Text(formatTime(durationSec), color = Color.White, fontSize = 12.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { isFullscreen = !isFullscreen; onFullScreenToggle?.invoke(isFullscreen) }, modifier = Modifier.size(42.dp).testTag("player_fullscreen_button")) {
                            Icon(if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Fullscreen", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

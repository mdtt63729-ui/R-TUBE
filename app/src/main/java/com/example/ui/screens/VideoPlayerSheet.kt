package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.model.AiInsightResult
import com.example.model.CommentItem
import com.example.model.NewPipeStreamInfo
import com.example.model.VideoItem
import com.example.ui.components.AnimatedLikeDislikePill
import com.example.ui.components.AnimatedPillButton
import com.example.ui.components.AnimatedSubscribeButton
import com.example.ui.components.VideoCard
import com.example.ui.player.PlayerController
import com.example.ui.player.YouTubePlayerView
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBackground
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubePurple
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun VideoPlayerSheet(
    video: VideoItem,
    relatedVideos: List<VideoItem>,
    comments: List<CommentItem>,
    aiInsight: AiInsightResult?,
    isAiLoading: Boolean,
    streamInfo: NewPipeStreamInfo? = null,
    controller: PlayerController,
    isFullscreen: Boolean = false,
    isStreamInfoLoading: Boolean = false,
    streamInfoError: String? = null,
    onFetchStreamInfo: () -> Unit = {},
    selectedQuality: String = "Auto",
    onQualitySelected: (String) -> Unit = {},
    onFullScreenToggle: (Boolean) -> Unit = {},
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onLikeToggle: () -> Unit,
    onWatchLaterToggle: () -> Unit,
    onDownloadClick: (VideoItem) -> Unit = {},
    onPostComment: (String) -> Unit,
    onAskAi: (String) -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isSubscribed by remember(video.channelId) { mutableStateOf(false) }
    var showAiPanel by remember { mutableStateOf(false) }
    var showPipedStreams by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }
    var aiQuestionInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YouTubeDarkBackground)
            .testTag("video_player_screen")
    ) {
        // Native YouTube Player
        Box(
            modifier = if (isFullscreen) {
                Modifier.fillMaxSize().background(Color.Black)
            } else {
                Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)
            }
        ) {
            val availableQualities = remember(streamInfo) {
                streamInfo?.videoStreams
                    ?.filter { it.url.isNotBlank() }
                    ?.map { it.quality.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    ?.sortedWith(compareByDescending<String> { Regex("(\\d{3,4})").find(it)?.groupValues?.get(1)?.toIntOrNull() ?: 0 })
                    .orEmpty()
            }

            YouTubePlayerView(
                videoId = video.id,
                controller = controller,
                streamInfo = streamInfo,
                streamInfoError = streamInfoError,
                onRetryStreamInfo = onFetchStreamInfo,
                selectedQuality = selectedQuality,
                availableQualities = availableQualities,
                onQualitySelected = onQualitySelected,
                onFullScreenToggle = onFullScreenToggle,
                onMinimize = onMinimize,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!isFullscreen) {
        // Scrollable Video Details & Related Feed
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Video Title & Metadata
            item {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = video.title,
                        color = YouTubeTextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${video.viewCountText} • ${video.publishedAt}",
                        color = YouTubeTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Channel Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = video.channelAvatarUrl,
                        contentDescription = video.channelTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(YouTubeDarkSurfaceElevated)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.channelTitle,
                            color = YouTubeTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1.2M subscribers",
                            color = YouTubeTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    AnimatedSubscribeButton(
                        isSubscribed = isSubscribed,
                        onSubscribeToggle = { isSubscribed = !isSubscribed }
                    )
                }
            }

            // Action Buttons Row: Like, Dislike, Share, Save, AI Copilot
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Animated Like / Dislike Pill
                    item {
                        AnimatedLikeDislikePill(
                            likeCountText = if (video.isLiked) "Liked" else video.likeCountText,
                            isLiked = video.isLiked,
                            isDisliked = false,
                            onLikeClick = {
                                onLikeToggle()
                                Toast.makeText(context, if (!video.isLiked) "Added to Liked Videos" else "Removed from Liked Videos", Toast.LENGTH_SHORT).show()
                            },
                            onDislikeClick = { }
                        )
                    }

                    // Download Button (Placed right after Like for instant access)
                    item {
                        AnimatedPillButton(
                            icon = Icons.Default.Download,
                            label = "Download",
                            onClick = { onDownloadClick(video) },
                            testTag = "player_download_button"
                        )
                    }

                    // Gemini AI Copilot Button
                    item {
                        AnimatedPillButton(
                            icon = Icons.Default.AutoAwesome,
                            label = "AI Copilot (Gemini 3.1 Pro)",
                            isActive = showAiPanel,
                            activeColor = YouTubePurple,
                            onClick = { showAiPanel = !showAiPanel },
                            testTag = "gemini_ai_copilot_button"
                        )
                    }

                    // Piped API Streams Button
                    item {
                        AnimatedPillButton(
                            icon = Icons.Default.Bolt,
                            label = "Piped Streams",
                            isActive = showPipedStreams,
                            activeColor = YouTubeRed,
                            onClick = {
                                showPipedStreams = !showPipedStreams
                                if (showPipedStreams && streamInfo == null) {
                                    onFetchStreamInfo()
                                }
                            },
                            testTag = "piped_streams_button"
                        )
                    }

                    // Save / Watch Later
                    item {
                        AnimatedPillButton(
                            icon = if (video.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            label = if (video.isSaved) "Saved" else "Save",
                            isActive = video.isSaved,
                            activeColor = YouTubeBlue,
                            onClick = {
                                onWatchLaterToggle()
                                Toast.makeText(context, if (!video.isSaved) "Saved to Watch Later" else "Removed from Watch Later", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "player_save_button"
                        )
                    }

                    // Share Button
                    item {
                        AnimatedPillButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this video: ${video.title} https://www.youtube.com/watch?v=${video.id}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share video"))
                            },
                            testTag = "player_share_button"
                        )
                    }
                }
            }

            // Gemini 3.1 Pro AI Copilot Panel (High Thinking Mode)
            if (showAiPanel) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YouTubeDarkSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("gemini_ai_panel")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = YouTubePurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini 3.1 Pro Thinking Insights",
                                        color = YouTubeTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Text(
                                    text = "HIGH THINKING",
                                    color = YouTubePurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .background(YouTubePurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isAiLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = YouTubePurple,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Reasoning deeply & synthesizing key takeaways...",
                                        color = YouTubeTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            } else if (aiInsight != null) {
                                Text(
                                    text = aiInsight.summary,
                                    color = YouTubeTextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                if (aiInsight.takeaways.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Key Takeaways:",
                                        color = YouTubeBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    aiInsight.takeaways.forEach { item ->
                                        Row(modifier = Modifier.padding(top = 4.dp)) {
                                            Text("• ", color = YouTubePurple, fontWeight = FontWeight.Bold)
                                            Text(item, color = YouTubeTextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }

                                if (aiInsight.keyMoments.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Chapters & Key Moments:",
                                        color = YouTubeBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    aiInsight.keyMoments.forEach { moment ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { }
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = moment.timestamp,
                                                color = YouTubeBlue,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = moment.title,
                                                color = YouTubeTextPrimary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Interactive Ask AI Box
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = aiQuestionInput,
                                    onValueChange = { aiQuestionInput = it },
                                    placeholder = { Text("Ask AI anything about this video...", fontSize = 12.sp, color = YouTubeTextSecondary) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = YouTubeDarkSurfaceVariant,
                                        unfocusedContainerColor = YouTubeDarkSurfaceVariant,
                                        focusedTextColor = YouTubeTextPrimary,
                                        unfocusedTextColor = YouTubeTextPrimary,
                                        focusedIndicatorColor = YouTubePurple,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (aiQuestionInput.isNotBlank()) {
                                            onAskAi(aiQuestionInput)
                                            aiQuestionInput = ""
                                        }
                                    })
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (aiQuestionInput.isNotBlank()) {
                                            onAskAi(aiQuestionInput)
                                            aiQuestionInput = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Ask",
                                        tint = YouTubePurple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Piped API Multi-Stream Inspector Panel
            if (showPipedStreams) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YouTubeDarkSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("piped_streams_panel")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = YouTubeRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Piped Stream Extractor",
                                        color = YouTubeTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Text(
                                    text = "API v0.24.4",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Source: ${streamInfo?.extractorSource ?: "Piped API → NewPipeExtractor"}",
                                color = YouTubeTextSecondary,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isStreamInfoLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = YouTubeRed,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Extracting available video/audio streams and manifests...",
                                        color = YouTubeTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            } else if (streamInfo != null) {
                                // Extracted Video Streams
                                Text(
                                    text = "DIRECT VIDEO STREAMS (${streamInfo.videoStreams.size})",
                                    color = YouTubeTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                streamInfo.videoStreams.take(5).forEach { stream ->
                                    Surface(
                                        color = YouTubeDarkSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.HighQuality,
                                                    contentDescription = null,
                                                    tint = if (stream.quality.contains("4K") || stream.quality.contains("2160")) YouTubeRed else YouTubeBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = stream.quality,
                                                        color = YouTubeTextPrimary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "${stream.format} • ${stream.fps}fps • ${stream.sizeMb} MB",
                                                        color = YouTubeTextSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(stream.url))
                                                        Toast.makeText(context, "Stream link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ContentCopy,
                                                        contentDescription = "Copy Stream URL",
                                                        tint = YouTubeTextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDownloadClick(video) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Download,
                                                        contentDescription = "Download Stream",
                                                        tint = YouTubeRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Extracted Audio Streams
                                Text(
                                    text = "EXTRACTED AUDIO STREAMS (${streamInfo.audioStreams.size})",
                                    color = YouTubeTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                streamInfo.audioStreams.take(3).forEach { aStream ->
                                    Surface(
                                        color = YouTubeDarkSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = aStream.quality,
                                                        color = YouTubeTextPrimary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "${aStream.format} • ${aStream.sizeMb} MB",
                                                        color = YouTubeTextSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(aStream.url))
                                                        Toast.makeText(context, "Audio stream link copied!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ContentCopy,
                                                        contentDescription = "Copy Audio URL",
                                                        tint = YouTubeTextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDownloadClick(video) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Download,
                                                        contentDescription = "Download Audio",
                                                        tint = YouTubeRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Description Box
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = YouTubeDarkSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            color = YouTubeTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = video.description.ifBlank { "No detailed description provided by creator." },
                            color = YouTubeTextPrimary,
                            fontSize = 12.sp,
                            maxLines = if (isDescriptionExpanded) 20 else 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 17.sp
                        )
                        Text(
                            text = if (isDescriptionExpanded) "...Show less" else "...more",
                            color = YouTubeTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Comments Box Header
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = YouTubeDarkSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Comments  ${video.commentsCount}",
                            color = YouTubeTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (comments.isEmpty()) "Join the conversation" else "Top comments from this video",
                            color = YouTubeTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        // Add Comment Input
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=User&background=E50914&color=fff&bold=true",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = commentInput,
                                onValueChange = { commentInput = it },
                                placeholder = { Text("Add a comment...", fontSize = 12.sp, color = YouTubeTextSecondary) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = YouTubeDarkSurfaceVariant,
                                    unfocusedContainerColor = YouTubeDarkSurfaceVariant,
                                    focusedTextColor = YouTubeTextPrimary,
                                    unfocusedTextColor = YouTubeTextPrimary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (commentInput.isNotBlank()) {
                                        onPostComment(commentInput)
                                        commentInput = ""
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    if (commentInput.isNotBlank()) {
                                        onPostComment(commentInput)
                                        commentInput = ""
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Post Comment",
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Top Comments preview
                        if (comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val topComment = comments.first()
                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = topComment.authorAvatarUrl,
                                    contentDescription = topComment.authorName,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${topComment.authorName} • ${topComment.publishedTime}",
                                        color = YouTubeTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = topComment.text,
                                        color = YouTubeTextPrimary,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Up Next / Related Videos Title
            item {
                Text(
                    text = "Up next",
                    color = YouTubeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }

            // Related Videos List
            items(relatedVideos, key = { it.id }) { item ->
                VideoCard(
                    video = item,
                    onClick = { onPlayVideo(item) },
                    onSaveWatchLater = { },
                    onShare = { }
                )
            }
        }
    }
    }
}

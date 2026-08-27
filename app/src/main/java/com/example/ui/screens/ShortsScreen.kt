package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.VideoItem
import com.example.ui.player.YouTubePlayerView
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun ShortsScreen(
    shorts: List<VideoItem>,
    onLikeToggle: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (shorts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No shorts loaded", color = Color.White)
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentShort = shorts.getOrElse(currentIndex) { shorts.first() }
    var isLiked by remember(currentShort.id) { mutableStateOf(false) }
    var isSubscribed by remember(currentShort.id) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_screen_container")
    ) {
        // Embedded Player for Short
        YouTubePlayerView(
            videoId = currentShort.id,
            isShort = true,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top Shorts Title & Switchers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shorts",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) currentIndex--
                    },
                    enabled = currentIndex > 0,
                    modifier = Modifier.testTag("prev_short_button")
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Previous Short",
                        tint = if (currentIndex > 0) Color.White else Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        if (currentIndex < shorts.size - 1) currentIndex++
                    },
                    enabled = currentIndex < shorts.size - 1,
                    modifier = Modifier.testTag("next_short_button")
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next Short",
                        tint = if (currentIndex < shorts.size - 1) Color.White else Color.Gray
                    )
                }
            }
        }

        // Bottom Left Info: Channel, Title, Audio
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            // Channel Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currentShort.channelAvatarUrl,
                    contentDescription = currentShort.channelTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "@${currentShort.channelTitle.replace(" ", "")}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { isSubscribed = !isSubscribed },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubscribed) Color.Gray.copy(alpha = 0.6f) else YouTubeRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isSubscribed) "Subscribed" else "Subscribe",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = currentShort.title,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sound Track
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Original Audio - ${currentShort.channelTitle}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        // Right Side Action Buttons: Like, Dislike, Comments, Share
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        onLikeToggle(currentShort)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("shorts_like_button")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) YouTubeRed else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = if (isLiked) "Liked" else currentShort.likeCountText,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Dislike
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(text = "Dislike", color = Color.White, fontSize = 11.sp)
            }

            // Comments
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(text = currentShort.commentsCount, color = Color.White, fontSize = 11.sp)
            }

            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onShare(currentShort) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(text = "Share", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

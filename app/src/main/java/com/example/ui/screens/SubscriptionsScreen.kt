package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SubscriptionEntity
import com.example.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.components.YouTubeTopAppBar
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionEntity>,
    feedVideos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onSaveWatchLater: (VideoItem) -> Unit,
    onShareVideo: (VideoItem) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        YouTubeTopAppBar(
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick
        )

        // Subscriptions Stories Avatar Row
        if (subscriptions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(subscriptions) { sub ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(60.dp)
                                .clickable { }
                                .testTag("channel_item_${sub.channelId}")
                        ) {
                            Box {
                                AsyncImage(
                                    model = sub.avatarUrl,
                                    contentDescription = sub.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(YouTubeDarkSurfaceElevated)
                                )
                                // Red unread dot
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(YouTubeBlue)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sub.name,
                                color = YouTubeTextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Text(
                    text = "ALL",
                    color = YouTubeBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { }
                )
            }
        }

        // Subscribed Feed List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .testTag("subscriptions_video_list")
        ) {
            items(feedVideos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoClick(video) },
                    onSaveWatchLater = { onSaveWatchLater(video) },
                    onShare = { onShareVideo(video) }
                )
            }
        }
    }
}

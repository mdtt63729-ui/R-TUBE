package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.SavedVideoEntity
import com.example.model.PlaylistItem
import com.example.model.VideoItem
import com.example.ui.components.YouTubeTopAppBar
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun LibraryScreen(
    history: List<HistoryEntity>,
    downloads: List<DownloadEntity> = emptyList(),
    likedVideos: List<SavedVideoEntity>,
    watchLater: List<SavedVideoEntity>,
    playlists: List<PlaylistItem>,
    onVideoClick: (VideoItem) -> Unit,
    onDeleteDownload: (String) -> Unit = {},
    onClearHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            YouTubeTopAppBar(
                onSearchClick = onSearchClick,
                onSettingsClick = onOpenSettings
            )
        }

        // Profile Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=Romi+User&background=E50914&color=fff&bold=true&size=256",
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(YouTubeDarkSurfaceElevated)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RomiTube User",
                        color = YouTubeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@user • RomiTube Ad-Free 4K Edition",
                        color = YouTubeTextSecondary,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = YouTubeTextPrimary
                    )
                }
            }
        }

        // Watch History Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    color = YouTubeTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (history.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = YouTubeBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onClearHistory() }
                            .testTag("clear_history_button")
                    )
                }
            }

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "No watch history yet. Videos you watch will appear here.",
                        color = YouTubeTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(history, key = { it.videoId }) { item ->
                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable {
                                    onVideoClick(
                                        VideoItem(
                                            id = item.videoId,
                                            title = item.title,
                                            channelTitle = item.channelTitle,
                                            thumbnailUrl = item.thumbnailUrl,
                                            duration = item.duration,
                                            viewCountText = item.viewCountText
                                        )
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = item.thumbnailUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = item.duration,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                color = YouTubeTextPrimary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.channelTitle,
                                color = YouTubeTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Downloads Section (Offline Ready)
        item {
            HorizontalDivider(
                color = YouTubeDarkSurfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Downloaded Videos (Offline)",
                        color = YouTubeTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${downloads.size} items",
                    color = YouTubeTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (downloads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "No downloaded videos yet. Click 'Download' on any video to watch in 4K offline without ads.",
                        color = YouTubeTextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    downloads.forEach { dl ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = YouTubeDarkSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onVideoClick(
                                        VideoItem(
                                            id = dl.videoId,
                                            title = dl.title,
                                            channelTitle = dl.channelTitle,
                                            thumbnailUrl = dl.thumbnailUrl,
                                            duration = dl.duration
                                        )
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 100.dp, height = 60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black)
                                ) {
                                    AsyncImage(
                                        model = dl.thumbnailUrl,
                                        contentDescription = dl.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = YouTubeRed.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 4.dp),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Downloaded",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp).padding(1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dl.title,
                                        color = YouTubeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = dl.channelTitle,
                                        color = YouTubeTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (dl.quality.contains("4K")) "4K UHD" else dl.quality.split(" ")[0],
                                                color = Color(0xFFFFD700),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${dl.fileSizeMb} MB",
                                            color = YouTubeTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteDownload(dl.videoId) }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Download",
                                        tint = YouTubeTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(
                color = YouTubeDarkSurfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Library Actions
        item {
            LibraryRowItem(
                icon = Icons.Default.PlaylistPlay,
                title = "Playlists",
                subtitle = "${playlists.size} playlists",
                onClick = { }
            )
            LibraryRowItem(
                icon = Icons.Default.ThumbUp,
                title = "Liked videos",
                subtitle = "${likedVideos.size} videos",
                onClick = { }
            )
            LibraryRowItem(
                icon = Icons.Default.Bookmark,
                title = "Watch Later",
                subtitle = "${watchLater.size} unwatched videos",
                onClick = { }
            )
            LibraryRowItem(
                icon = Icons.Default.Download,
                title = "Offline Downloads Manager",
                subtitle = "${downloads.size} offline videos • Ad-Free Playback",
                onClick = { }
            )
            LibraryRowItem(
                icon = Icons.Default.VpnKey,
                title = "YouTube API & AI Settings",
                subtitle = "Configure Custom API Key & Gemini 3.1 Pro",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
fun LibraryRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = YouTubeTextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = YouTubeTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = YouTubeTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}


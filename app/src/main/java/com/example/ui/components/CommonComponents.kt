package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.CategoryItem
import com.example.model.VideoItem
import com.example.ui.theme.YouTubeDarkBackground
import com.example.ui.theme.YouTubeDarkSurface
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun YouTubeTopAppBar(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // RomiTube Logo with Custom Generated Emblem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_logo_container")
        ) {
            Image(
                painter = painterResource(id = R.drawable.romitube_logo_1787642443472),
                contentDescription = "RomiTube Logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RomiTube",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = YouTubeTextPrimary
            )
        }

        // Action Icons: Cast, Notifications, Search, Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("cast_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = "Cast",
                    tint = YouTubeTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("notification_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = YouTubeTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("search_icon_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = YouTubeTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = YouTubeTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<CategoryItem>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.name == selectedCategory
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "chip_scale"
            )

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else YouTubeDarkSurfaceVariant,
                animationSpec = tween(150),
                label = "chip_bg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.Black else YouTubeTextPrimary,
                animationSpec = tween(150),
                label = "chip_text"
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                modifier = Modifier
                    .scale(scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onCategorySelected(category.name) }
                    .testTag("category_chip_${category.name.lowercase()}")
            ) {
                Text(
                    text = category.name,
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onSaveWatchLater: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .bouncingClickable { onClick() }
            .testTag("video_card_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = YouTubeDarkSurface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail with Duration Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Black)
            ) {
                var thumbnailFailed by remember(video.thumbnailUrl) { mutableStateOf(false) }
                AsyncImage(
                    model = if (thumbnailFailed) {
                        "https://i.ytimg.com/vi/${video.id}/hqdefault.jpg"
                    } else {
                        video.thumbnailUrl.ifBlank { "https://i.ytimg.com/vi/${video.id}/hqdefault.jpg" }
                    },
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    onError = {
                        if (!thumbnailFailed) thumbnailFailed = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Duration Pill
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Details Row: Channel Avatar, Title, Metadata, 3-dot Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Channel Avatar
                AsyncImage(
                    model = video.channelAvatarUrl,
                    contentDescription = video.channelTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(YouTubeDarkSurfaceElevated)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Channel / Views Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = video.title,
                        color = YouTubeTextPrimary,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${video.channelTitle} • ${video.viewCountText} • ${video.publishedAt}",
                        color = YouTubeTextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3-dots Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("video_menu_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = YouTubeTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(YouTubeDarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save to Watch later", color = YouTubeTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = YouTubeTextPrimary)
                            },
                            onClick = {
                                showMenu = false
                                onSaveWatchLater()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share video", color = YouTubeTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null, tint = YouTubeTextPrimary)
                            },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                    }
                }
            }
        }
    }
}

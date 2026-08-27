package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.player.PlayerController
import coil.compose.AsyncImage
import com.example.model.VideoItem
import com.example.ui.theme.YouTubeDarkBackground
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary
import com.example.viewmodel.ScreenTab

@Composable
fun YouTubeBottomNavigation(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("bottom_navigation_bar"),
        containerColor = YouTubeDarkBackground,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == ScreenTab.HOME,
            onClick = { onTabSelected(ScreenTab.HOME) },
            icon = {
                AnimatedContent(
                    targetState = currentTab == ScreenTab.HOME,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "home_icon_transition"
                ) { selected ->
                    Icon(
                        imageVector = if (selected) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                }
            },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeTextPrimary,
                selectedTextColor = YouTubeTextPrimary,
                indicatorColor = Color.Transparent,
                unselectedIconColor = YouTubeTextSecondary,
                unselectedTextColor = YouTubeTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentTab == ScreenTab.SHORTS,
            onClick = { onTabSelected(ScreenTab.SHORTS) },
            icon = {
                AnimatedContent(
                    targetState = currentTab == ScreenTab.SHORTS,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "shorts_icon_transition"
                ) { selected ->
                    Icon(
                        imageVector = if (selected) Icons.Filled.Whatshot else Icons.Outlined.Whatshot,
                        contentDescription = "Shorts"
                    )
                }
            },
            label = { Text("Shorts", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeRed,
                selectedTextColor = YouTubeTextPrimary,
                indicatorColor = Color.Transparent,
                unselectedIconColor = YouTubeTextSecondary,
                unselectedTextColor = YouTubeTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_shorts")
        )

        NavigationBarItem(
            selected = currentTab == ScreenTab.SEARCH,
            onClick = { onTabSelected(ScreenTab.SEARCH) },
            icon = {
                AnimatedContent(
                    targetState = currentTab == ScreenTab.SEARCH,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "search_icon_transition"
                ) { selected ->
                    Icon(
                        imageVector = if (selected) Icons.Filled.Search else Icons.Outlined.Search,
                        contentDescription = "Explore"
                    )
                }
            },
            label = { Text("Search", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeTextPrimary,
                selectedTextColor = YouTubeTextPrimary,
                indicatorColor = Color.Transparent,
                unselectedIconColor = YouTubeTextSecondary,
                unselectedTextColor = YouTubeTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_search")
        )

        NavigationBarItem(
            selected = currentTab == ScreenTab.SUBSCRIPTIONS,
            onClick = { onTabSelected(ScreenTab.SUBSCRIPTIONS) },
            icon = {
                AnimatedContent(
                    targetState = currentTab == ScreenTab.SUBSCRIPTIONS,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "subscriptions_icon_transition"
                ) { selected ->
                    Icon(
                        imageVector = if (selected) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                        contentDescription = "Subscriptions"
                    )
                }
            },
            label = { Text("Subscriptions", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeTextPrimary,
                selectedTextColor = YouTubeTextPrimary,
                indicatorColor = Color.Transparent,
                unselectedIconColor = YouTubeTextSecondary,
                unselectedTextColor = YouTubeTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_subscriptions")
        )

        NavigationBarItem(
            selected = currentTab == ScreenTab.LIBRARY,
            onClick = { onTabSelected(ScreenTab.LIBRARY) },
            icon = {
                AnimatedContent(
                    targetState = currentTab == ScreenTab.LIBRARY,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "library_icon_transition"
                ) { selected ->
                    Icon(
                        imageVector = if (selected) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                        contentDescription = "Library"
                    )
                }
            },
            label = { Text("You", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YouTubeTextPrimary,
                selectedTextColor = YouTubeTextPrimary,
                indicatorColor = Color.Transparent,
                unselectedIconColor = YouTubeTextSecondary,
                unselectedTextColor = YouTubeTextSecondary
            ),
            modifier = Modifier.testTag("nav_item_library")
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun MiniPlayerBar(
    video: VideoItem,
    controller: PlayerController,
    onExpand: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(76.dp).testTag("mini_player_bar"),
        color = YouTubeDarkSurfaceElevated,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(76.dp).clickable { onExpand() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AndroidView(
                modifier = Modifier.size(width = 104.dp, height = 60.dp).clip(RoundedCornerShape(8.dp)),
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        player = controller.player
                    }
                },
                update = { it.player = controller.player }
            )

            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(video.title, color = YouTubeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(video.channelTitle, color = YouTubeTextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = onExpand, modifier = Modifier.size(40.dp).testTag("mini_player_play_button")) {
                Icon(Icons.Default.PlayArrow, "Open player", tint = YouTubeTextPrimary)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(40.dp).testTag("mini_player_close_button")) {
                Icon(Icons.Default.Close, "Close player", tint = YouTubeTextPrimary)
            }
        }
    }
}


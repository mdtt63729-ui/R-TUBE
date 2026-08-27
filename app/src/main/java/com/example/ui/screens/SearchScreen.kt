package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SearchHistoryEntity
import com.example.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoCardSkeleton
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: List<VideoItem>,
    searchSuggestions: List<String> = emptyList(),
    recentSearches: List<SearchHistoryEntity>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit = {},
    onSearch: (String) -> Unit,
    onDeleteRecentSearch: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onSaveWatchLater: (VideoItem) -> Unit,
    onShareVideo: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf(searchQuery) }
    val focusManager = LocalFocusManager.current

    val trendingTopics = listOf(
        "Latest Hindi Songs 2026",
        "Android App Development Jetpack Compose",
        "Cricket World Cup Live",
        "Gemini 3.1 Pro AI Deep Dive",
        "MrBeast Real Life Challenge",
        "Top Minecraft Mods",
        "Bollywood Movies Trailer",
        "Python Coding in 10 Minutes"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header Bar (M3 pill search bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = {
                    textInput = it
                    onQueryChange(it)
                },
                placeholder = {
                    Text("Search RomiTube...", color = YouTubeTextSecondary, fontSize = 15.sp)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (textInput.isNotEmpty()) YouTubeRed else YouTubeTextSecondary
                    )
                },
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = {
                            textInput = ""
                            onQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = YouTubeTextSecondary)
                        }
                    } else {
                        IconButton(onClick = { /* Voice search action */ }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = YouTubeTextSecondary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearch(textInput)
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = YouTubeDarkSurfaceVariant,
                    unfocusedContainerColor = YouTubeDarkSurfaceVariant,
                    focusedTextColor = YouTubeTextPrimary,
                    unfocusedTextColor = YouTubeTextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = YouTubeRed
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input")
            )
        }

        // Live Auto-Suggestions (if user is typing and suggestions are available)
        if (textInput.isNotBlank() && searchSuggestions.isNotEmpty() && !isSearching && searchResults.isEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(searchSuggestions) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                textInput = suggestion
                                focusManager.clearFocus()
                                onSearch(suggestion)
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = YouTubeTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = suggestion,
                            color = YouTubeTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = "Complete",
                            tint = YouTubeTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        } else if (isSearching) {
            // Shimmer Loading Skeletons
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
                    .testTag("search_loading_skeletons")
            ) {
                items(4) {
                    VideoCardSkeleton(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        } else if (searchResults.isNotEmpty()) {
            // Search Results List
            val firstVideo = searchResults.firstOrNull()
            val potentialChannelName = firstVideo?.channelTitle ?: searchQuery
            val isChannelLikeQuery = searchQuery.contains("gaming", ignoreCase = true) ||
                    searchQuery.contains("official", ignoreCase = true) ||
                    searchQuery.contains("channel", ignoreCase = true) ||
                    searchQuery.contains("mrbeast", ignoreCase = true) ||
                    searchQuery.contains("t-series", ignoreCase = true) ||
                    searchQuery.contains("carryminati", ignoreCase = true) ||
                    searchQuery.contains("jonathan", ignoreCase = true) ||
                    searchQuery.split(" ").size <= 2

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list")
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Results for \"$searchQuery\"",
                            color = YouTubeTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${searchResults.size} videos found",
                            color = YouTubeRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Official YouTube Channel Search Card
                if (firstVideo != null && isChannelLikeQuery) {
                    item(key = "channel_card_header") {
                        var isSubscribedToChannel by remember(firstVideo.channelTitle) { mutableStateOf(false) }
                        Surface(
                            color = YouTubeDarkSurfaceElevated,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                coil.compose.AsyncImage(
                                    model = firstVideo.channelAvatarUrl,
                                    contentDescription = firstVideo.channelTitle,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(YouTubeDarkSurfaceVariant)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firstVideo.channelTitle,
                                        color = YouTubeTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "@${firstVideo.channelTitle.replace(" ", "").lowercase()}",
                                        color = YouTubeTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Official Channel • 1.2M+ subscribers",
                                        color = YouTubeTextSecondary.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                com.example.ui.components.AnimatedSubscribeButton(
                                    isSubscribed = isSubscribedToChannel,
                                    onSubscribeToggle = { isSubscribedToChannel = !isSubscribedToChannel }
                                )
                            }
                        }
                    }
                }

                items(searchResults, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onSaveWatchLater = { onSaveWatchLater(video) },
                        onShare = { onShareVideo(video) }
                    )
                }
            }
        } else {
            // Recent Searches & Trending Suggestions
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Searches",
                            color = YouTubeTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    items(recentSearches) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    textInput = item.query
                                    focusManager.clearFocus()
                                    onSearch(item.query)
                                }
                                .padding(vertical = 10.dp)
                                .testTag("recent_search_${item.query}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = YouTubeTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = item.query,
                                color = YouTubeTextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteRecentSearch(item.query) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = YouTubeTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trending Searches",
                            color = YouTubeTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        trendingTopics.forEach { topic ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = YouTubeDarkSurfaceElevated,
                                modifier = Modifier.bouncingClickable {
                                    textInput = topic
                                    focusManager.clearFocus()
                                    onSearch(topic)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = YouTubeRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = topic,
                                        color = YouTubeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
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

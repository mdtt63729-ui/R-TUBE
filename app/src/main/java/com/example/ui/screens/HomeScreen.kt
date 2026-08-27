package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CategoryItem
import com.example.model.VideoItem
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoCardSkeleton
import com.example.ui.components.YouTubeTopAppBar
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    categories: List<CategoryItem>,
    selectedCategory: String,
    isLoading: Boolean,
    isLoadingMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onCategorySelected: (String) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onSaveWatchLater: (VideoItem) -> Unit,
    onShareVideo: (VideoItem) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Detect when user scrolls near the bottom to trigger endless infinite loading
    val shouldLoadMore by remember(videos.size, isLoading, isLoadingMore) {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading && !isLoadingMore) {
            onLoadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // RomiTube Top App Bar
        YouTubeTopAppBar(
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick
        )

        // M3 Categories Row
        CategoryChipsRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                // Sleek Material 3 Shimmer Skeletons
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .testTag("home_shimmer_skeletons")
                ) {
                    items(4) {
                        VideoCardSkeleton()
                    }
                }
            } else if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos available in this category.",
                        color = YouTubeTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_video_list")
                ) {
                    items(videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onSaveWatchLater = { onSaveWatchLater(video) },
                            onShare = { onShareVideo(video) }
                        )
                    }

                    // Bottom loader indicator for infinite scrolling
                    if (isLoadingMore) {
                        item(key = "footer_loader") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = YouTubeRed,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

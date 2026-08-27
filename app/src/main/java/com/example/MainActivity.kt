package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DownloadBottomSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.PullToRefreshContainer
import com.example.ui.components.YouTubeBottomNavigation
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.VideoPlayerSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.YouTubeDarkBackground
import com.example.viewmodel.ScreenTab
import com.example.viewmodel.YouTubeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: YouTubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                YouTubeAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun YouTubeAppContent(viewModel: YouTubeViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val homeVideos by viewModel.homeVideos.collectAsStateWithLifecycle()
    val categories = viewModel.categories
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isHomeLoading by viewModel.isHomeLoading.collectAsStateWithLifecycle()
    val isHomeLoadingMore by viewModel.isHomeLoadingMore.collectAsStateWithLifecycle()

    val shortsList by viewModel.shortsList.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val subscribedChannels by viewModel.subscribedChannels.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()
    val watchLaterVideos by viewModel.watchLaterVideos.collectAsStateWithLifecycle()
    val playlists = viewModel.playlists

    val activeVideo by viewModel.activeVideo.collectAsStateWithLifecycle()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()
    val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
    val activeComments by viewModel.activeComments.collectAsStateWithLifecycle()
    val relatedVideos by viewModel.relatedVideos.collectAsStateWithLifecycle()
    val aiInsight by viewModel.aiInsight.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle()
    val downloadTargetVideo by viewModel.downloadTargetVideo.collectAsStateWithLifecycle()

    val streamInfo by viewModel.streamInfo.collectAsStateWithLifecycle()
    val isStreamInfoLoading by viewModel.isStreamInfoLoading.collectAsStateWithLifecycle()
    val streamInfoError by viewModel.streamInfoError.collectAsStateWithLifecycle()
    val isNewPipeExtractorEnabled by viewModel.isNewPipeExtractorEnabled.collectAsStateWithLifecycle()
    val extractorLatency by viewModel.extractorLatency.collectAsStateWithLifecycle()

    val customPipedInstance by viewModel.customPipedInstance.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()

    // Fullscreen back first exits fullscreen; second back minimizes the player.
    BackHandler(enabled = isPlayerExpanded) {
        if (isFullscreen) {
            viewModel.setFullscreen(false)
            val activity = context as? ComponentActivity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.let {
                WindowInsetsControllerCompat(it.window, it.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            viewModel.minimizePlayer()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(YouTubeDarkBackground)) {
        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(YouTubeDarkBackground)
            .safeDrawingPadding(),
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mini player bar if video is active and not expanded
                if (activeVideo != null && !isPlayerExpanded) {
                    MiniPlayerBar(
                        video = activeVideo!!,
                        controller = viewModel.playerController,
                        onExpand = { viewModel.expandPlayer() },
                        onClose = { viewModel.closePlayer() }
                    )
                }

                if (!isPlayerExpanded) {
                    YouTubeBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { tab -> viewModel.selectTab(tab) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PullToRefreshContainer(
                refreshing = isHomeLoading || isSearching,
                onRefresh = { viewModel.refreshCurrentTab() },
                modifier = Modifier.fillMaxSize()
            ) {
            // Lightweight Material 3-style directional transitions.
            AnimatedContent(
                targetState = currentTab,
                label = "main_tab_transition",
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    if (forward) {
                        (slideInHorizontally(tween(220)) { it / 4 } + fadeIn(tween(180))) togetherWith
                            (slideOutHorizontally(tween(220)) { -it / 5 } + fadeOut(tween(140)))
                    } else {
                        (slideInHorizontally(tween(220)) { -it / 4 } + fadeIn(tween(180))) togetherWith
                            (slideOutHorizontally(tween(220)) { it / 5 } + fadeOut(tween(140)))
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { tab ->
            when (tab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        videos = homeVideos,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        isLoading = isHomeLoading,
                        isLoadingMore = isHomeLoadingMore,
                        onLoadMore = { viewModel.loadMoreHomeVideos() },
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onVideoClick = { viewModel.playVideo(it) },
                        onSaveWatchLater = { viewModel.toggleWatchLaterActiveVideo() },
                        onShareVideo = { },
                        onSearchClick = { viewModel.selectTab(ScreenTab.SEARCH) },
                        onSettingsClick = { viewModel.toggleSettingsDialog(true) }
                    )
                }

                ScreenTab.SHORTS -> {
                    ShortsScreen(
                        shorts = shortsList,
                        onLikeToggle = { viewModel.toggleLikeActiveVideo() },
                        onShare = { }
                    )
                }

                ScreenTab.SEARCH -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        searchSuggestions = searchSuggestions,
                        recentSearches = recentSearches,
                        isSearching = isSearching,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onSearch = { viewModel.performSearch(it) },
                        onDeleteRecentSearch = { viewModel.deleteRecentSearch(it) },
                        onVideoClick = { viewModel.playVideo(it) },
                        onSaveWatchLater = { viewModel.toggleWatchLaterActiveVideo() },
                        onShareVideo = { }
                    )
                }

                ScreenTab.SUBSCRIPTIONS -> {
                    SubscriptionsScreen(
                        subscriptions = subscribedChannels,
                        feedVideos = homeVideos,
                        onVideoClick = { viewModel.playVideo(it) },
                        onSaveWatchLater = { viewModel.toggleWatchLaterActiveVideo() },
                        onShareVideo = { },
                        onSearchClick = { viewModel.selectTab(ScreenTab.SEARCH) },
                        onSettingsClick = { viewModel.toggleSettingsDialog(true) }
                    )
                }

                ScreenTab.LIBRARY -> {
                    LibraryScreen(
                        history = watchHistory,
                        downloads = downloads,
                        likedVideos = likedVideos,
                        watchLater = watchLaterVideos,
                        playlists = playlists,
                        onVideoClick = { viewModel.playVideo(it) },
                        onDeleteDownload = { viewModel.deleteDownload(it) },
                        onClearHistory = { viewModel.clearAllHistory() },
                        onOpenSettings = { viewModel.toggleSettingsDialog(true) },
                        onSearchClick = { viewModel.selectTab(ScreenTab.SEARCH) }
                    )
                }
            }
            }
            }

            // Expanded Video Player Modal Sheet
            AnimatedVisibility(
                visible = activeVideo != null && isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                if (activeVideo != null) {
                    VideoPlayerSheet(
                        video = activeVideo!!,
                        relatedVideos = relatedVideos,
                        comments = activeComments,
                        aiInsight = aiInsight,
                        isAiLoading = isAiLoading,
                        streamInfo = streamInfo,
                        controller = viewModel.playerController,
                        isFullscreen = isFullscreen,
                        isStreamInfoLoading = isStreamInfoLoading,
                        streamInfoError = streamInfoError,
                        onFetchStreamInfo = { viewModel.fetchStreamDetails(activeVideo!!.id) },
                        selectedQuality = videoQuality,
                        onQualitySelected = { viewModel.setVideoQuality(it) },
                        onFullScreenToggle = { fullscreen ->
                            viewModel.setFullscreen(fullscreen)
                            val activity = context as? ComponentActivity ?: return@VideoPlayerSheet
                            activity.requestedOrientation = if (fullscreen) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                            if (fullscreen) {
                                controller.hide(WindowInsetsCompat.Type.systemBars())
                                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            } else {
                                controller.show(WindowInsetsCompat.Type.systemBars())
                            }
                        },
                        onMinimize = { viewModel.minimizePlayer() },
                        onClose = { viewModel.closePlayer() },
                        onLikeToggle = { viewModel.toggleLikeActiveVideo() },
                        onWatchLaterToggle = { viewModel.toggleWatchLaterActiveVideo() },
                        onDownloadClick = { viewModel.promptDownload(it) },
                        onPostComment = { viewModel.postComment(it) },
                        onAskAi = { viewModel.analyzeVideo(activeVideo!!, it) },
                        onPlayVideo = { viewModel.playVideo(it) }
                    )
                }
            }

            // Download Bottom Sheet Dialog
            downloadTargetVideo?.let { targetVideo ->
                DownloadBottomSheet(
                    video = targetVideo,
                    onDismiss = { viewModel.dismissDownloadPrompt() },
                    onStartDownload = { quality, sizeMb ->
                        viewModel.startDownload(targetVideo, quality, sizeMb)
                    }
                )
            }

            // Settings Dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    currentPipedInstance = customPipedInstance,
                    isNewPipeEnabled = isNewPipeExtractorEnabled,
                    extractorLatency = extractorLatency,
                    onToggleNewPipe = { viewModel.toggleNewPipeExtractor(it) },
                    onPingNewPipe = { viewModel.pingExtractor() },
                    onSavePipedInstance = { viewModel.saveCustomPipedInstance(it) },
                    onClearHistory = { viewModel.clearAllHistory() },
                    onDismiss = { viewModel.toggleSettingsDialog(false) }
                )
            }
        }
        }


    }
}
package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val duration: String,
    val viewCountText: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val progressSeconds: Int = 0
)

@Entity(tableName = "saved_videos")
data class SavedVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val duration: String,
    val viewCountText: String,
    val savedAt: Long = System.currentTimeMillis(),
    val type: String = "WATCH_LATER" // "LIKED" or "WATCH_LATER" or "PLAYLIST"
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val avatarUrl: String,
    val handle: String,
    val subscriberCount: String,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val duration: String,
    val quality: String = "1080p Full HD",
    val fileSizeMb: Double = 95.0,
    val progress: Int = 100,
    val isCompleted: Boolean = true,
    val downloadedAt: Long = System.currentTimeMillis()
)


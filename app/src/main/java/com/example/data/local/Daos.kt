package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteHistoryItem(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}

@Dao
interface SavedVideoDao {
    @Query("SELECT * FROM saved_videos WHERE type = :type ORDER BY savedAt DESC")
    fun getSavedVideosByType(type: String): Flow<List<SavedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedVideo(item: SavedVideoEntity)

    @Query("DELETE FROM saved_videos WHERE videoId = :videoId AND type = :type")
    suspend fun removeSavedVideo(videoId: String, type: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_videos WHERE videoId = :videoId AND type = :type)")
    fun isSaved(videoId: String, type: String): Flow<Boolean>
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(item: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelId = :channelId")
    suspend fun deleteSubscription(channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelId = :channelId)")
    fun isSubscribed(channelId: String): Flow<Boolean>
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(item: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadEntity)

    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE videoId = :videoId)")
    fun isDownloaded(videoId: String): Flow<Boolean>

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
}


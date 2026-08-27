package com.example.model

data class VideoItem(
    val id: String,
    val title: String,
    val channelTitle: String,
    val channelId: String = "",
    val channelAvatarUrl: String = "",
    val description: String = "",
    val publishedAt: String = "Just now",
    val duration: String = "10:24",
    val viewCountText: String = "1.2M views",
    val likeCountText: String = "45K",
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val isShort: Boolean = false,
    val category: String = "All",
    val commentsCount: String = "1.4K",
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

data class CommentItem(
    val id: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val text: String,
    val publishedTime: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false
)

data class ChannelItem(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val bannerUrl: String = "",
    val subscriberCountText: String,
    val videoCountText: String = "120 videos",
    val isSubscribed: Boolean = false,
    val description: String = ""
)

data class CategoryItem(
    val id: String,
    val name: String,
    val query: String = ""
)

data class PlaylistItem(
    val id: String,
    val title: String,
    val videoCount: Int,
    val thumbnailUrl: String,
    val isPrivate: Boolean = true,
    val updatedAt: String = "Recently updated"
)

data class AiInsightResult(
    val summary: String,
    val keyMoments: List<KeyMoment> = emptyList(),
    val takeaways: List<String> = emptyList(),
    val sentiment: String = "Informative & Engaging",
    val qnaAnswers: Map<String, String> = emptyMap()
)

data class KeyMoment(
    val timestamp: String,
    val title: String,
    val description: String = ""
)

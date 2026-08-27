package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

/**
 * Animated Pill Action Button with interactive bounce feedback
 */
@Composable
fun AnimatedPillButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: Color = YouTubeRed,
    testTag: String = "animated_pill_btn"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.22f) else YouTubeDarkSurfaceElevated,
        animationSpec = tween(durationMillis = 200),
        label = "pill_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isActive) activeColor else YouTubeTextPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "pill_content_color"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Animated Like/Dislike Dual Pill
 */
@Composable
fun AnimatedLikeDislikePill(
    likeCountText: String,
    isLiked: Boolean,
    isDisliked: Boolean,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_scale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = YouTubeDarkSurfaceElevated,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Like Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLikeClick() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("like_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = if (isLiked) YouTubeRed else YouTubeTextPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .scale(likeScale)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = likeCountText,
                    color = if (isLiked) YouTubeRed else YouTubeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .size(width = 1.dp, height = 18.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // Dislike Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDislikeClick() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag("dislike_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = "Dislike",
                    tint = if (isDisliked) YouTubeRed else YouTubeTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Animated Subscribe Button with sleek morphing states
 */
@Composable
fun AnimatedSubscribeButton(
    isSubscribed: Boolean,
    onSubscribeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sub_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSubscribeToggle() }
            .testTag("subscribe_button"),
        shape = RoundedCornerShape(20.dp),
        color = if (isSubscribed) YouTubeDarkSurfaceVariant else Color.White
    ) {
        AnimatedContent(
            targetState = isSubscribed,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
            label = "sub_content"
        ) { subscribed ->
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subscribed) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Subscribed",
                        tint = YouTubeTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Subscribed",
                        color = YouTubeTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "Subscribe",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.VideoItem
import com.example.ui.theme.YouTubeBlue
import com.example.ui.theme.YouTubeDarkBackground
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

data class DownloadQualityOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val sizeText: String,
    val sizeMb: Double,
    val icon: ImageVector,
    val isAudioOnly: Boolean = false,
    val is4k: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadBottomSheet(
    video: VideoItem,
    onDismiss: () -> Unit,
    onStartDownload: (quality: String, sizeMb: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val downloadOptions = remember {
        listOf(
            DownloadQualityOption(
                id = "4k",
                title = "4K Ultra HD (2160p 60fps)",
                subtitle = "Crisp Ultra Cinema Quality • HDR",
                sizeText = "480 MB",
                sizeMb = 480.0,
                icon = Icons.Default.HighQuality,
                is4k = true
            ),
            DownloadQualityOption(
                id = "2k",
                title = "2K Quad HD (1440p)",
                subtitle = "Very High Resolution",
                sizeText = "220 MB",
                sizeMb = 220.0,
                icon = Icons.Default.HighQuality
            ),
            DownloadQualityOption(
                id = "1080p",
                title = "Full HD (1080p 60fps)",
                subtitle = "Recommended for Phones & Tablets",
                sizeText = "95 MB",
                sizeMb = 95.0,
                icon = Icons.Default.OndemandVideo
            ),
            DownloadQualityOption(
                id = "720p",
                title = "High Definition (720p)",
                subtitle = "Faster Download • Good Quality",
                sizeText = "45 MB",
                sizeMb = 45.0,
                icon = Icons.Default.OndemandVideo
            ),
            DownloadQualityOption(
                id = "480p",
                title = "Standard Definition (480p)",
                subtitle = "Data Saver",
                sizeText = "22 MB",
                sizeMb = 22.0,
                icon = Icons.Default.SdCard
            ),
            DownloadQualityOption(
                id = "mp3",
                title = "Audio Only (MP3 HQ 320kbps)",
                subtitle = "Crystal Clear Audio • Music & Podcasts",
                sizeText = "8.5 MB",
                sizeMb = 8.5,
                icon = Icons.Default.Audiotrack,
                isAudioOnly = true
            )
        )
    }

    var selectedOption by remember { mutableStateOf(downloadOptions[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = YouTubeDarkSurfaceElevated,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Video",
                        tint = YouTubeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download Video (Offline)",
                        color = YouTubeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = YouTubeTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Preview Mini Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(YouTubeDarkSurfaceVariant)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier
                        .size(width = 80.dp, height = 48.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = YouTubeTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${video.channelTitle} • ${video.duration}",
                        color = YouTubeTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SELECT DOWNLOAD RESOLUTION / QUALITY",
                color = YouTubeTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Quality Options List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                downloadOptions.forEach { option ->
                    val isSelected = selectedOption.id == option.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) YouTubeDarkBackground else YouTubeDarkSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) (if (option.is4k) Color(0xFFFFD700) else YouTubeRed) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedOption = option }
                            .testTag("download_option_${option.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedOption = option },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (option.is4k) Color(0xFFFFD700) else YouTubeRed,
                                    unselectedColor = YouTubeTextSecondary
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = if (option.is4k) Color(0xFFFFD700) else (if (option.isAudioOnly) YouTubeBlue else YouTubeTextPrimary),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = option.title,
                                        color = YouTubeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (option.is4k) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ULTRA HD",
                                                color = Color(0xFFFFD700),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = option.subtitle,
                                    color = YouTubeTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = option.sizeText,
                                color = if (isSelected) YouTubeRed else YouTubeTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    onStartDownload(selectedOption.title, selectedOption.sizeMb)
                    android.widget.Toast.makeText(
                        context,
                        "Downloading in ${selectedOption.title.split(" ")[0]} (${selectedOption.sizeText})... Saved to Library Downloads!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_download_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YouTubeRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download in ${selectedOption.title.split(" ")[0]} (${selectedOption.sizeText})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

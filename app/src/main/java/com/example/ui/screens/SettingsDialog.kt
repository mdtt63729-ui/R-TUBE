package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeDarkSurfaceElevated
import com.example.ui.theme.YouTubeDarkSurfaceVariant
import com.example.ui.theme.YouTubePurple
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeTextPrimary
import com.example.ui.theme.YouTubeTextSecondary

@Composable
fun SettingsDialog(
    currentPipedInstance: String,
    isNewPipeEnabled: Boolean = true,
    extractorLatency: Long = 48L,
    onToggleNewPipe: (Boolean) -> Unit = {},
    onPingNewPipe: () -> Unit = {},
    onSavePipedInstance: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var instanceInput by remember { mutableStateOf(currentPipedInstance) }
    var npEnabled by remember { mutableStateOf(isNewPipeEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "RomiTube & Extractor Settings",
                    color = YouTubeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // NewPipe Extractor / LibreTube-style API Card
                Surface(
                    color = YouTubeDarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "NewPipe Extractor / LibreTube-style API",
                                        color = YouTubeTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (npEnabled) "NewPipe direct extraction active" else "Disabled",
                                        color = if (npEnabled) Color(0xFF4CAF50) else YouTubeTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = npEnabled,
                                onCheckedChange = {
                                    npEnabled = it
                                    onToggleNewPipe(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = YouTubeRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Fetches search, feeds, metadata, comments, subtitles, related videos, and playable streams directly with NewPipe Extractor on the device.",
                            color = YouTubeTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (extractorLatency > 0) Color(0xFF4CAF50) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (extractorLatency > 0) "Ping: ${extractorLatency}ms (Online)" else "Checking instance...",
                                    color = YouTubeTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = onPingNewPipe,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Test Latency",
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Extractor Mode",
                    color = YouTubeTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RomiTube now uses NewPipe Extractor directly on-device, matching LibreTube's extraction stack without depending on unstable public Piped instances.",
                    color = YouTubeTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = instanceInput,
                    onValueChange = { instanceInput = it },
                    placeholder = { Text("https://pipedapi.example.com", color = YouTubeTextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = YouTubeDarkSurfaceVariant,
                        unfocusedContainerColor = YouTubeDarkSurfaceVariant,
                        focusedTextColor = YouTubeTextPrimary,
                        unfocusedTextColor = YouTubeTextPrimary,
                        focusedIndicatorColor = YouTubeRed,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("piped_instance_input_field")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Gemini 3.1 Pro info
                Surface(
                    color = YouTubePurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = YouTubePurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Gemini 3.1 Pro Active",
                                color = YouTubeTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "High thinking mode enabled for deep video analysis & Q&A.",
                                color = YouTubeTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Clear History Button
                OutlinedButton(
                    onClick = {
                        onClearHistory()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Watch & Search History", color = YouTubeRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSavePipedInstance(instanceInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = YouTubeTextSecondary)
            }
        },
        containerColor = YouTubeDarkSurfaceElevated
    )
}


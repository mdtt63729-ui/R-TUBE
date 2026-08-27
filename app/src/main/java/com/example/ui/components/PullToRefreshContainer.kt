package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.YouTubeRed

@Composable
fun PullToRefreshContainer(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        var lastY = down.position.y
                        var total = 0f
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            val dy = change.position.y - lastY
                            lastY = change.position.y
                            if (dy > 0f) {
                                total = (total + dy).coerceAtMost(180f)
                                dragDistance = total
                                armed = total >= 92f
                            } else if (total > 0f) {
                                total = (total + dy).coerceAtLeast(0f)
                                dragDistance = total
                                armed = total >= 92f
                            }
                        }
                        if (armed && !refreshing) onRefresh()
                        dragDistance = 0f
                        armed = false
                    }
                }
            }
    ) {
        content()
        AnimatedVisibility(
            visible = dragDistance > 8f || refreshing,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(shape = CircleShape, tonalElevation = 8.dp, shadowElevation = 8.dp, modifier = Modifier.padding(top = 10.dp).size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (refreshing) {
                        // Indeterminate spinner while the refresh network call is in flight —
                        // this is the version that actually rotates continuously.
                        CircularProgressIndicator(
                            color = YouTubeRed,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        // Static arc that fills in as the user drags down, before release.
                        CircularProgressIndicator(
                            progress = { (dragDistance / 120f).coerceIn(0.08f, 1f) },
                            color = YouTubeRed,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.example.webcamstreaming.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.webcamstreaming.data.Webcam
import com.example.webcamstreaming.ui.components.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitscreenScreen(
    webcams: List<Webcam>,
    modifier: Modifier = Modifier,
    onCameraClick: (Webcam) -> Unit,
    audioMuted: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 4 fixed slots for the grid; allows the user to fill empty tiles via bottom sheet selection.
    val slots = remember(webcams) {
        val initial = MutableList<Webcam?>(4) { null }
        webcams.take(4).forEachIndexed { index, webcam -> initial[index] = webcam }
        initial
    }

    var openSlotIndex by remember { mutableStateOf<Int?>(null) }

    // When webcams shrink (e.g., deletion), clear slots pointing to removed webcams.
    LaunchedEffect(webcams.map { it.id }) {
        val ids = webcams.map { it.id }.toSet()
        for (i in 0 until 4) {
            val current = slots[i]
            if (current != null && !ids.contains(current.id)) {
                slots[i] = null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            key(slots[0]?.id ?: "empty_0") {
                SplitscreenCell(
                    webcam = slots[0],
                    onEmptyClick = { openSlotIndex = 0 },
                    onCameraClick = { onCameraClick(it) },
                    audioMuted = audioMuted,
                )
            }
            key(slots[1]?.id ?: "empty_1") {
                SplitscreenCell(
                    webcam = slots[1],
                    onEmptyClick = { openSlotIndex = 1 },
                    onCameraClick = { onCameraClick(it) },
                    audioMuted = audioMuted,
                )
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            key(slots[2]?.id ?: "empty_2") {
                SplitscreenCell(
                    webcam = slots[2],
                    onEmptyClick = { openSlotIndex = 2 },
                    onCameraClick = { onCameraClick(it) },
                    audioMuted = audioMuted,
                )
            }
            key(slots[3]?.id ?: "empty_3") {
                SplitscreenCell(
                    webcam = slots[3],
                    onEmptyClick = { openSlotIndex = 3 },
                    onCameraClick = { onCameraClick(it) },
                    audioMuted = audioMuted,
                )
            }
        }
    }

    if (openSlotIndex != null) {
        ModalBottomSheet(
            onDismissRequest = { openSlotIndex = null },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Kamera auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                webcams.forEach { webcam ->
                    ListItem(
                        headlineContent = { Text(text = webcam.name) },
                        supportingContent = { Text(text = webcam.streamUrl, maxLines = 1) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera icon",
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                slots[openSlotIndex!!] = webcam
                                openSlotIndex = null
                                scope.launch { sheetState.hide() }
                            }
                    )
                }
            }
        }
        LaunchedEffect(openSlotIndex) {
            scope.launch { sheetState.show() }
        }
    }
}

@Composable
private fun RowScope.SplitscreenCell(
    webcam: Webcam?,
    onEmptyClick: () -> Unit,
    onCameraClick: (Webcam) -> Unit,
    audioMuted: Boolean,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        if (webcam != null) {
            key("${webcam.id}_${webcam.streamUrl}") {
                VideoPlayer(
                    url = webcam.streamUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                        .clickable { onCameraClick(webcam) },
                    audioMuted = audioMuted,
                )
            }
            Text(
                text = webcam.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                    .clickable(onClick = onEmptyClick),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "No camera icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Tippe zum Auswählen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.webcamstreaming.data.Webcam
import com.example.webcamstreaming.ui.components.VideoPlayer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitscreenScreen(
    webcams: List<Webcam>,
    slotAssignments: List<Webcam?>,
    onSlotSelected: (slotIndex: Int, webcam: Webcam) -> Unit,
    onClearSlot: (slotIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    onCameraClick: (Webcam) -> Unit,
    audioMuted: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val slots = remember(slotAssignments) {
        slotAssignments.take(4).let { list ->
            if (list.size < 4) list + List(4 - list.size) { null } else list
        }
    }

    var openSlotIndex by remember { mutableStateOf<Int?>(null) }

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
                    slotIndex = 0,
                    webcam = slots[0],
                    onOpenPicker = { openSlotIndex = it },
                    onCameraClick = onCameraClick,
                    audioMuted = audioMuted,
                )
            }
            key(slots[1]?.id ?: "empty_1") {
                SplitscreenCell(
                    slotIndex = 1,
                    webcam = slots[1],
                    onOpenPicker = { openSlotIndex = it },
                    onCameraClick = onCameraClick,
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
                    slotIndex = 2,
                    webcam = slots[2],
                    onOpenPicker = { openSlotIndex = it },
                    onCameraClick = onCameraClick,
                    audioMuted = audioMuted,
                )
            }
            key(slots[3]?.id ?: "empty_3") {
                SplitscreenCell(
                    slotIndex = 3,
                    webcam = slots[3],
                    onOpenPicker = { openSlotIndex = it },
                    onCameraClick = onCameraClick,
                    audioMuted = audioMuted,
                )
            }
        }
    }

    val pickerSlotIndex = openSlotIndex
    if (pickerSlotIndex != null) {
        ModalBottomSheet(
            onDismissRequest = { openSlotIndex = null },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Kamera auswählen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (slots[pickerSlotIndex] != null) {
                    TextButton(
                        onClick = {
                            onClearSlot(pickerSlotIndex)
                            openSlotIndex = null
                            scope.launch { sheetState.hide() }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Feld leeren")
                    }
                }
                if (webcams.isEmpty()) {
                    Text(
                        text = "Keine Kameras gespeichert",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        items(webcams, key = { it.id }) { webcam ->
                            ListItem(
                                headlineContent = { Text(text = webcam.name) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSlotSelected(pickerSlotIndex, webcam)
                                        openSlotIndex = null
                                        scope.launch { sheetState.hide() }
                                    }
                            )
                        }
                    }
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
    slotIndex: Int,
    webcam: Webcam?,
    onOpenPicker: (Int) -> Unit,
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
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onOpenPicker(slotIndex) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
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

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.45f),
            shadowElevation = 2.dp
        ) {
            IconButton(
                onClick = { onOpenPicker(slotIndex) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SwitchCamera,
                    contentDescription = "Kamera wählen",
                    tint = Color.White
                )
            }
        }
    }
}

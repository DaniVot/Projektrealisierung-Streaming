package com.example.webcamstreaming.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webcamstreaming.data.Webcam
import com.example.webcamstreaming.ui.components.StreamStatus
import com.example.webcamstreaming.ui.components.StreamStatusDot
import com.example.webcamstreaming.ui.components.StreamStatusProbe

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    webcams: List<Webcam>,
    onWebcamClick: (Webcam) -> Unit,
    onWebcamLongClick: (Webcam) -> Unit = {},
    onWebcamDelete: (Webcam) -> Unit = {},
    onAddExampleCameras: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (webcams.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "No webcams",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "Keine Webcams gespeichert",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Tippe auf + um eine hinzuzufügen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = onAddExampleCameras,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Beispiel-Kameras hinzufügen")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        content = {
            items(webcams, key = { it.id }) { webcam ->
                var status by remember(webcam.id) { mutableStateOf(StreamStatus.BUFFERING) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onWebcamClick(webcam) },
                                onLongClick = { onWebcamLongClick(webcam) }
                            )
                    ) {
                        // Background probe for the status dot (no video rendering, just stream readiness).
                        StreamStatusProbe(
                            url = webcam.streamUrl,
                            onStatusChanged = { status = it }
                        )

                        StreamStatusDot(
                            status = status,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = webcam.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Hinzugefügt: ${formatAddedAt(webcam.addedAt)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                        .copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { onWebcamDelete(webcam) }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete webcam",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun formatAddedAt(addedAt: Long): String {
    // dd.MM.yyyy
    return remember(addedAt) {
        if (addedAt <= 0L) return@remember "Unbekannt"
        val df = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        df.format(java.util.Date(addedAt))
    }
}

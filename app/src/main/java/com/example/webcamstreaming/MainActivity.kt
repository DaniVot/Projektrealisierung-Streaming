package com.example.webcamstreaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webcamstreaming.data.Webcam
import com.example.webcamstreaming.ui.screens.AddWebcamDialog
import com.example.webcamstreaming.ui.screens.DetailScreen
import com.example.webcamstreaming.ui.screens.ListScreen
import com.example.webcamstreaming.ui.screens.MainScreen
import com.example.webcamstreaming.ui.screens.SplitscreenScreen
import com.example.webcamstreaming.ui.theme.WebcamStreamingTheme
import com.example.webcamstreaming.ui.viewmodel.WebcamViewModel
import com.example.webcamstreaming.ui.viewmodel.WebcamViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebcamStreamingTheme {
                val viewModel: WebcamViewModel = viewModel(
                    factory = WebcamViewModelFactory(application)
                )
                val webcams by viewModel.webcams.collectAsState()
                val selectedDetailId by viewModel.selectedDetailId
                val globalAudioMuted by viewModel.globalAudioMuted
                val detailWebcam = remember(selectedDetailId, webcams) {
                    selectedDetailId?.let { id -> webcams.find { it.id == id } }
                }
                var showAddDialog by remember { mutableStateOf(false) }
                var webcamToEdit by remember { mutableStateOf<Webcam?>(null) }

                detailWebcam?.let { w ->
                    key("${w.id}_${w.streamUrl}") {
                        DetailScreen(
                            webcam = w,
                            onBack = { viewModel.closeDetail() },
                            onUserMessage = { viewModel.showMessage(it) },
                            isAudioMuted = globalAudioMuted,
                            onToggleAudioMute = { viewModel.toggleGlobalAudioMute() }
                        )
                    }
                } ?: run {
                    MainScreen(
                        viewModel = viewModel,
                        cameraCount = webcams.size,
                        listContent = { modifier ->
                            ListScreen(
                                webcams = webcams,
                                onWebcamClick = { viewModel.openDetail(it) },
                                onWebcamLongClick = { webcam ->
                                    webcamToEdit = webcam
                                },
                                onWebcamDelete = { webcam ->
                                    viewModel.removeWebcam(webcam)
                                },
                                modifier = modifier,
                                onAddExampleCameras = { viewModel.addSampleWebcams() }
                            )
                        },
                        splitscreenContent = { modifier ->
                            SplitscreenScreen(
                                webcams = webcams,
                                modifier = modifier,
                                onCameraClick = { viewModel.openDetail(it) },
                                audioMuted = globalAudioMuted
                            )
                        },
                        onAddClick = { showAddDialog = true }
                    )
                }

                if (showAddDialog) {
                    AddWebcamDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { name, url ->
                            viewModel.addWebcam(name, url)
                            showAddDialog = false
                        },
                        validateUrl = { viewModel.isValidUrl(it) }
                    )
                }
                webcamToEdit?.let { w ->
                    AddWebcamDialog(
                        initialName = w.name,
                        initialUrl = w.streamUrl,
                        title = "Edit webcam",
                        onDismiss = { webcamToEdit = null },
                        onConfirm = { name, url ->
                            viewModel.updateWebcam(w.copy(name = name, streamUrl = url))
                            webcamToEdit = null
                        },
                        validateUrl = { viewModel.isValidUrl(it) }
                    )
                }
            }
        }
    }
}

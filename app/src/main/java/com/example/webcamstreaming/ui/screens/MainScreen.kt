package com.example.webcamstreaming.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webcamstreaming.ui.viewmodel.WebcamViewModel
import com.example.webcamstreaming.ui.viewmodel.WebcamViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WebcamViewModel = viewModel(
        factory = WebcamViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    ),
    cameraCount: Int,
    listContent: @Composable (Modifier) -> Unit,
    splitscreenContent: @Composable (Modifier) -> Unit,
    onAddClick: () -> Unit
) {
    val snackbarMessage by viewModel.snackbarMessage
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var overflowExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            val result = snackbarHostState.showSnackbar(
                message = msg.message,
                actionLabel = msg.actionLabel,
                duration = if (msg.offersDeleteUndo) SnackbarDuration.Long else SnackbarDuration.Short,
                withDismissAction = true
            )
            if (msg.offersDeleteUndo) {
                when (result) {
                    SnackbarResult.ActionPerformed -> viewModel.confirmDeleteUndo()
                    SnackbarResult.Dismissed -> viewModel.discardDeleteUndo()
                }
            }
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Webcam Streaming (${cameraCount})") },
                actions = {
                    IconButton(
                        onClick = { overflowExpanded = true },
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Über") },
                            onClick = {
                                overflowExpanded = false
                                showAboutDialog = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add webcam")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("List") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Splitscreen") }
                )
            }
            when (selectedTabIndex) {
                0 -> listContent(Modifier.fillMaxSize())
                1 -> splitscreenContent(Modifier.fillMaxSize())
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Über") },
            text = {
                Column {
                    Text("Webcam Streaming", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Version: 1.0",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Entwickler: Silas Hartje und Daniel Votler",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

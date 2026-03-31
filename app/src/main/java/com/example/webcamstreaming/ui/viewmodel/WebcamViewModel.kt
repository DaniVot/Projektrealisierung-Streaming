package com.example.webcamstreaming.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webcamstreaming.data.Webcam
import com.example.webcamstreaming.data.WebcamRepository
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SnackbarMessage(val message: String, val isError: Boolean = false)

class WebcamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WebcamRepository(application)

    val webcams = repository.webcamsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Resolved webcams for splitscreen grid (4 slots), from persisted IDs + current list. */
    val splitscreenSlots = combine(
        repository.webcamsFlow,
        repository.splitscreenSlotIdsFlow
    ) { cams, ids ->
        ids.map { id -> id?.let { wid -> cams.find { it.id == wid } } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(null, null, null, null)
    )

    var snackbarMessage = mutableStateOf<SnackbarMessage?>(null)
        private set

    /** Selected webcam id for detail view. Resolve actual webcam from [webcams] by this id to avoid stale references. */
    var selectedDetailId = mutableStateOf<String?>(null)
        private set

    fun openDetail(webcam: Webcam) {
        selectedDetailId.value = webcam.id
    }

    fun closeDetail() {
        selectedDetailId.value = null
    }

    /** Global audio mute for all players (detail + splitscreen). false = unmuted (volume 1f). */
    var globalAudioMuted = mutableStateOf(false)
        private set

    fun toggleGlobalAudioMute() {
        globalAudioMuted.value = !globalAudioMuted.value
    }

    fun setSplitscreenSlot(index: Int, webcam: Webcam?) {
        require(index in 0..3)
        viewModelScope.launch {
            repository.setSplitscreenSlot(index, webcam?.id)
        }
    }

    fun addWebcam(name: String, url: String) {
        viewModelScope.launch {
            repository.addWebcam(name, url)
                .onSuccess {
                    snackbarMessage.value = SnackbarMessage("Webcam hinzugefügt")
                }
                .onFailure { e ->
                    val msg = when (e.message) {
                        "invalid_url" -> "Bitte eine gültige Stream-URL eingeben (z.B. https://example.com/stream.m3u8)"
                        else -> e.message ?: "Failed to add webcam"
                    }
                    snackbarMessage.value = SnackbarMessage(msg, isError = true)
                }
        }
    }

    fun removeWebcam(webcam: Webcam) {
        viewModelScope.launch {
            repository.removeWebcam(webcam.id)
            snackbarMessage.value = SnackbarMessage("Webcam gelöscht")
        }
    }

    fun updateWebcam(webcam: Webcam) {
        viewModelScope.launch {
            repository.updateWebcam(webcam)
                .onSuccess {
                    snackbarMessage.value = SnackbarMessage("Webcam aktualisiert")
                }
                .onFailure { e ->
                    val msg = when (e.message) {
                        "invalid_url" -> "Bitte eine gültige Stream-URL eingeben (z.B. https://example.com/stream.m3u8)"
                        else -> e.message ?: "Failed to update webcam"
                    }
                    snackbarMessage.value = SnackbarMessage(msg, isError = true)
                }
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    fun showMessage(message: String) {
        snackbarMessage.value = SnackbarMessage(message)
    }

    fun isValidUrl(url: String?): Boolean = repository.isValidStreamUrl(url)
}

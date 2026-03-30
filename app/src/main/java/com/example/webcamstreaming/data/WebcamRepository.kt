package com.example.webcamstreaming.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WebcamRepository(context: Context) {

    private val dataStore = WebcamDataStore(context)

    val webcamsFlow: Flow<List<Webcam>> = dataStore.webcamsFlow.map { list ->
        list.sortedByDescending { it.addedAt }
    }

    suspend fun addWebcam(name: String, streamUrl: String): Result<Unit> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be empty"))
        if (!isValidStreamUrl(streamUrl)) return Result.failure(IllegalArgumentException("invalid_url"))
        val webcam = Webcam(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            streamUrl = streamUrl.trim(),
            addedAt = System.currentTimeMillis()
        )
        return runCatching { dataStore.addWebcam(webcam) }
    }

    suspend fun removeWebcam(id: String) {
        dataStore.removeWebcam(id)
    }

    suspend fun updateWebcam(webcam: Webcam): Result<Unit> {
        if (webcam.name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be empty"))
        if (!isValidStreamUrl(webcam.streamUrl)) return Result.failure(IllegalArgumentException("invalid_url"))
        return runCatching { dataStore.updateWebcam(webcam) }
    }

    fun isValidStreamUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return try {
            val u = java.net.URL(url.trim())
            (u.protocol == "http" || u.protocol == "https") && u.host.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}

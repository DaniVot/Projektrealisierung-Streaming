package com.example.webcamstreaming.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "webcams")

private val webcamListSerializer = ListSerializer(Webcam.serializer())

private val json = Json {
    ignoreUnknownKeys = true
}

private object Keys {
    val WEBCAM_LIST = stringPreferencesKey("webcam_list")
    val SPLIT_POS_0 = stringPreferencesKey("splitscreen_pos_0")
    val SPLIT_POS_1 = stringPreferencesKey("splitscreen_pos_1")
    val SPLIT_POS_2 = stringPreferencesKey("splitscreen_pos_2")
    val SPLIT_POS_3 = stringPreferencesKey("splitscreen_pos_3")
}

private val splitscreenPosKeys = listOf(
    Keys.SPLIT_POS_0,
    Keys.SPLIT_POS_1,
    Keys.SPLIT_POS_2,
    Keys.SPLIT_POS_3,
)

class WebcamDataStore(private val context: Context) {

    val webcamsFlow: Flow<List<Webcam>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.WEBCAM_LIST] ?: "[]"
        try {
            json.decodeFromString(webcamListSerializer, raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Up to four saved webcam IDs for splitscreen grid positions (null = empty slot). */
    val splitscreenSlotIdsFlow: Flow<List<String?>> = context.dataStore.data.map { prefs ->
        splitscreenPosKeys.map { key ->
            prefs[key]?.takeIf { it.isNotBlank() }
        }
    }

    suspend fun setSplitscreenSlot(index: Int, webcamId: String?) {
        require(index in 0..3)
        context.dataStore.edit { prefs ->
            val key = splitscreenPosKeys[index]
            if (webcamId.isNullOrBlank()) {
                prefs.remove(key)
            } else {
                prefs[key] = webcamId
            }
        }
    }

    suspend fun saveWebcams(webcams: List<Webcam>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEBCAM_LIST] = json.encodeToString(webcamListSerializer, webcams)
        }
    }

    /** Returns the currently stored list (same source as webcamsFlow). Never overwrites on decode failure. */
    suspend fun getCurrentWebcams(): List<Webcam> {
        val raw = context.dataStore.data.first()[Keys.WEBCAM_LIST] ?: "[]"
        return try {
            json.decodeFromString(webcamListSerializer, raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun addWebcam(webcam: Webcam) {
        val current = getCurrentWebcams()
        val newList = current + webcam
        context.dataStore.edit { prefs ->
            prefs[Keys.WEBCAM_LIST] = json.encodeToString(webcamListSerializer, newList)
        }
    }

    suspend fun updateWebcam(webcam: Webcam) {
        val current = getCurrentWebcams()
        val newList = current.map { if (it.id == webcam.id) webcam else it }
        if (newList.any { it.id == webcam.id }) {
            context.dataStore.edit { prefs ->
                prefs[Keys.WEBCAM_LIST] = json.encodeToString(webcamListSerializer, newList)
            }
        }
    }

    suspend fun removeWebcam(id: String) {
        val current = getCurrentWebcams()
        val newList = current.filter { it.id != id }
        context.dataStore.edit { prefs ->
            prefs[Keys.WEBCAM_LIST] = json.encodeToString(webcamListSerializer, newList)
            for (key in splitscreenPosKeys) {
                if (prefs[key] == id) {
                    prefs.remove(key)
                }
            }
        }
    }
}

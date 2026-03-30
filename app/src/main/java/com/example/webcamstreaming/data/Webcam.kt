package com.example.webcamstreaming.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Model for a saved webcam with name, stream URL and timestamp.
 */
@Serializable
data class Webcam(
    val id: String,
    val name: String,
    val streamUrl: String,
    val addedAt: Long
) {
    fun toJson(): String = Json.encodeToString(this)
    companion object {
        fun fromJson(json: String): Webcam = Json.decodeFromString(json)
    }
}

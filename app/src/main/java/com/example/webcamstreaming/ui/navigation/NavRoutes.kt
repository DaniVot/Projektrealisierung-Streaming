package com.example.webcamstreaming.ui.navigation

import com.example.webcamstreaming.data.Webcam

sealed class NavRoute {
    data object List : NavRoute()
    data object Splitscreen : NavRoute()
    data class Detail(val webcam: Webcam) : NavRoute()
}

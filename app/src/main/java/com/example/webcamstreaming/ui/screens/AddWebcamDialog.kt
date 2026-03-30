package com.example.webcamstreaming.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalClipboardManager

@Composable
fun AddWebcamDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String) -> Unit,
    validateUrl: (String?) -> Boolean,
    initialName: String = "",
    initialUrl: String = "",
    title: String = "Add webcam"
) {
    var name by remember { mutableStateOf(initialName) }
    var url by remember { mutableStateOf(initialUrl) }
    var nameError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    fun isExpectedUrlFormat(value: String?): Boolean {
        val v = value?.trim().orEmpty()
        if (v.isBlank()) return false
        val startsWithHttp = v.startsWith("http://") || v.startsWith("https://")
        val containsM3u8 = v.contains(".m3u8") || v.endsWith(".m3u8") || v.contains("m3u8")
        return startsWithHttp && containsM3u8
    }

    val isRealtimeValid = isExpectedUrlFormat(url) && validateUrl(url)

    LaunchedEffect(initialName, initialUrl) {
        name = initialName
        url = initialUrl
        urlError = initialUrl.isNotBlank() && !(isExpectedUrlFormat(initialUrl) && validateUrl(initialUrl))
    }

    Dialog(onDismissRequest = onDismiss) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameError
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            urlError = !isExpectedUrlFormat(it) || !validateUrl(it)
                        },
                        label = { Text("Stream URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        trailingIcon = {
                            val iconColor = if (urlError) Color(0xFFB00020) else Color(0xFF2E7D32)
                            if (url.isBlank()) {
                                Text("")
                            } else {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (urlError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                                        contentDescription = if (urlError) "Invalid URL" else "Valid URL",
                                        tint = iconColor
                                    )
                                }
                            }
                        },
                        supportingText = {
                            Column {
                                if (urlError) {
                                    Text("Ungültige URL. Erwartet: https://example.com/stream.m3u8")
                                } else if (title == "Edit webcam") {
                                    Text("URLs (z.B. SkylineWebcams) können ablaufen; kopiere eine frische URL aus DevTools, falls der Stream fehlschlägt.")
                                } else {
                                    Text("Erwartet: https://example.com/stream.m3u8")
                                }
                            }
                        },
                        isError = urlError
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = {
                                val pasted = clipboardManager.getText()?.text.orEmpty()
                                if (pasted.isNotBlank()) {
                                    url = pasted
                                    urlError = !isExpectedUrlFormat(pasted) || !validateUrl(pasted)
                                }
                            },
                            modifier = Modifier.padding(start = 0.dp)
                        ) {
                            androidx.compose.foundation.layout.Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Filled.ContentPaste, contentDescription = "Paste URL")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("URL einfügen")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        nameError = name.isBlank()
                        urlError = !isRealtimeValid
                        if (!nameError && !urlError) {
                            onConfirm(name.trim(), url.trim())
                            onDismiss()
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

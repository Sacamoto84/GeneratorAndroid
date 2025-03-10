package com.example.generator2.screens.html

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScreenHtml() {
    val state = rememberWebViewState("file:///android_asset/html/_.htm")
    WebView(state, onCreated = { it.settings.javaScriptEnabled = true })
}




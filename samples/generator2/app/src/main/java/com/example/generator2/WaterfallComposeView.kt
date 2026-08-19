package com.example.generator2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/** Водопад спектра во View, завёрнутый для Compose-экрана. */
@Composable
fun WaterfallComposeView() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(color = Color.Black),
        factory = { context -> WaterfallView(context, null) },
    )
}

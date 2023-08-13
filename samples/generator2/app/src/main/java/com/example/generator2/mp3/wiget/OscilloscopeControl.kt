package com.example.generator2.mp3.wiget

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.generator2.mp3.stream.OSCILLSYNC
import com.example.generator2.mp3.stream.compressorCount
import com.example.generator2.mp3.stream.oscillSync


@Composable
fun OscilloscopeControl() {

    Row {

        Button(onClick = { compressorCount.floatValue *= 2 }) {

        }
        Text(text = "${compressorCount.floatValue}", color = Color.White)
        Button(onClick = { compressorCount.floatValue /= 2 }) {

        }

        Button(onClick = { oscillSync.value = OSCILLSYNC.NONE }) {
            Text(text = "N")
        }
        Button(onClick = { oscillSync.value = OSCILLSYNC.R }) {
            Text(text = "R")
        }
        Button(onClick = { oscillSync.value = OSCILLSYNC.L }) {
            Text(text = "L")
        }
    }
}
package com.example.generator2.mp3.wiget

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.generator2.mp3.stream.compressorCount


@Composable
fun OscilloscopeControl() {

    Row {
        Button(onClick = { compressorCount.intValue +=1}) {

        }
        Text(text = "${compressorCount.intValue}", color = Color.White)
        Button(onClick = { compressorCount.intValue -= 1 }) {

        }
    }
}
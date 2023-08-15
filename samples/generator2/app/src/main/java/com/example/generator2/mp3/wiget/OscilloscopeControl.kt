package com.example.generator2.mp3.wiget

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.generator2.audio_device.audioOutBT
import com.example.generator2.audio_device.audioOutSpeaker
import com.example.generator2.audio_device.audioOutWired
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.stream.compressorCount
import com.example.generator2.mp3.oscillSync


@Composable
fun OscilloscopeControl() {

    val context = LocalContext.current

    Row {

        Button(onClick = { compressorCount.floatValue *= 2 }) {

        }
        Text(text = "${compressorCount.floatValue}", color = Color.White)
        Button(onClick = { compressorCount.floatValue /= 2 }) {

        }

//        Button(onClick = { oscillSync.value = OSCILLSYNC.NONE }) {
//            Text(text = "N")
//        }
//        Button(onClick = { oscillSync.value = OSCILLSYNC.R }) {
//            Text(text = "R")
//        }
//        Button(onClick = { oscillSync.value = OSCILLSYNC.L }) {
//            Text(text = "L")
//        }

        Button(onClick = { audioOutSpeaker(context) }) {
            Text(text = "S")
        }
        Button(onClick = { audioOutWired(context) }) {
            Text(text = "W")
        }
        Button(onClick = { audioOutBT(context) }) {
            Text(text = "B")
        }

    }
}
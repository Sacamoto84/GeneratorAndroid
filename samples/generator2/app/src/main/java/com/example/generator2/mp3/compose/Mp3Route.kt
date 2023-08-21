package com.example.generator2.mp3.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.audio.ROUTESTREAM
import com.example.generator2.audio.audioMixerPump

@Composable
fun Mp3Route(ch: String = "R", route: ROUTESTREAM) {
    Row {
        Text(
            text = "MP3",
            color = if (route == ROUTESTREAM.MP3) Color.Green else Color.Gray,
            modifier = Modifier.clickable(onClick = {
                if (ch == "R")
                    audioMixerPump.routeR.value = ROUTESTREAM.MP3
                else
                    audioMixerPump.routeL.value = ROUTESTREAM.MP3
            })
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Gen",
            color = if (route == ROUTESTREAM.GEN) Color.Green else Color.Gray,
            modifier = Modifier.clickable(onClick = {
                if (ch == "R")
                    audioMixerPump.routeR.value = ROUTESTREAM.GEN
                else
                    audioMixerPump.routeL.value = ROUTESTREAM.GEN
            }
            )
        )
    }


}
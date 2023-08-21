package com.example.generator2.mp3.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.generator2.audio.ROUTESTREAM
import com.example.generator2.audio.audioMixerPump

@Composable
fun Mp3Route(ch: String = "R", route: ROUTESTREAM) {
    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {

       Box(contentAlignment = Alignment.Center, modifier = Modifier
           .size(32.dp)
           .background(Color.Cyan)
       ) {
           Text(
               text = "$ch",
               color = Color.DarkGray,
               textAlign = TextAlign.Center
           )
       }


        Text(
            text = "MP3",
            color = if (route == ROUTESTREAM.MP3) Color.Green else Color.Gray,
            modifier = Modifier
                .clickable(onClick = {
                    if (ch == "R")
                        audioMixerPump.routeR.value = ROUTESTREAM.MP3
                    else
                        audioMixerPump.routeL.value = ROUTESTREAM.MP3
                })
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Gen",
            color = if (route == ROUTESTREAM.GEN) Color.Green else Color.Gray,
            modifier = Modifier
                .clickable(onClick = {
                    if (ch == "R")
                        audioMixerPump.routeR.value = ROUTESTREAM.GEN
                    else
                        audioMixerPump.routeL.value = ROUTESTREAM.GEN
                }
                )
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Off",
            color = if (route == ROUTESTREAM.OFF) Color.Green else Color.Gray,
            modifier = Modifier
                .clickable(onClick = {
                    if (ch == "R")
                        audioMixerPump.routeR.value = ROUTESTREAM.OFF
                    else
                        audioMixerPump.routeL.value = ROUTESTREAM.OFF
                }
                )
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )


    }


}
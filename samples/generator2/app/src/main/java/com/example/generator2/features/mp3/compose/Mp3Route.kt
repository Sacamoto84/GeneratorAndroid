package com.example.generator2.features.mp3.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.audio.ROUTESTREAM
import com.example.generator2.theme.colorGreen
import com.example.generator2.theme.colorOrange


@Composable
fun Mp3Route(ch: String = "R", route: ROUTESTREAM, audioMixerPump: AudioMixerPump) {

    val h = 32.dp

    Row(modifier = Modifier.padding(top = 0.dp), verticalAlignment = Alignment.CenterVertically) {


        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(h)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .background(if (ch == "L") colorGreen else colorOrange)
        ) {
            Text(
                text = ch,
                color = Color.Black,
                textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 20.sp
            )
        }

        //Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "MP3",
            color = if (route == ROUTESTREAM.MP3) Color.Green else Color.Gray,
            modifier = Modifier
                .height(h)
                .clickable(onClick = {
                    if (ch == "R")
                        audioMixerPump.routeR.value = ROUTESTREAM.MP3
                    else
                        audioMixerPump.routeL.value = ROUTESTREAM.MP3
                })
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        )
        //Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "GEN",
            color = if (route == ROUTESTREAM.GEN) Color.Green else Color.Gray,
            modifier = Modifier
                .height(h)
                .offset(x = (-1).dp)
                .clickable(onClick = {
                    if (ch == "R")
                        audioMixerPump.routeR.value = ROUTESTREAM.GEN
                    else
                        audioMixerPump.routeL.value = ROUTESTREAM.GEN
                }
                )
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        )

        //Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "OFF",
            color = if (route == ROUTESTREAM.OFF) Color.Green else Color.Gray,
            modifier = Modifier
                .height(h)
                .offset(x = (-2).dp)
                .clickable(onClick = {
                    if (ch == "R")
                        audioMixerPump.routeR.value = ROUTESTREAM.OFF
                    else
                        audioMixerPump.routeL.value = ROUTESTREAM.OFF
                }
                )
                .border(1.dp, Color.Gray, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .padding(8.dp)

        )

        Spacer(modifier = Modifier.width(16.dp))

        val colorInvert =
            if (ch == "R") audioMixerPump.invertR.collectAsState().value else audioMixerPump.invertL.collectAsState().value

        Icon(
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    onClick = {
                        if (ch == "R") audioMixerPump.invertR.value =
                            audioMixerPump.invertR.value.not() else audioMixerPump.invertL.value =
                            audioMixerPump.invertL.value.not()
                    }
                )
                .border(1.dp, Color.Gray)
                .padding(4.dp),
            painter = painterResource(id = R.drawable.arrow_up_arrow_down51),
            contentDescription = "",
            tint = if (colorInvert) Color.Green else Color.DarkGray
        )


    }


}
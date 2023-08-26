package com.example.generator2.mp3.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.generator2.NavigationRoute
import com.example.generator2.R
import com.example.generator2.audio.AudioSampleRate
import com.example.generator2.audio.audioMixerPump
import com.example.generator2.mp3.exoplayer
import com.example.generator2.mp3.formatMinSec
import com.example.generator2.navController
import com.example.generator2.theme.Purple200


@Composable
fun MP3Control() {
    Column {

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "P:${exoplayer.currentTime.collectAsState().value.formatMinSec()}",
                color = Color.Yellow
            )
            Text(
                text = "D:${exoplayer.durationMs.collectAsState().value.formatMinSec()}",
                color = Color.Yellow
            )
        }

        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = exoplayer.currentTime.collectAsState().value.toFloat(),
            onValueChange = { timeMs: Float -> exoplayer.player.seekTo(timeMs.toLong()) },
            valueRange = 0f..exoplayer.durationMs.collectAsState().value.toFloat(),
            colors = SliderDefaults.colors(thumbColor = Purple200, activeTickColor = Purple200)
        )


        /////////////////////кнопки/////////////////////



        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {




            IconButton(modifier = Modifier.size(40.dp), onClick = {  navController.navigate(NavigationRoute.EXPLORER.value) }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(modifier = Modifier.size(40.dp), onClick = {}) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_backward_step),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(modifier = Modifier.size(40.dp), onClick = { exoplayer.player.play() }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_play),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = { exoplayer.player.pause() }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_pause),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(modifier = Modifier.size(40.dp), onClick = { exoplayer.player.stop() }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_stop),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(modifier = Modifier.size(40.dp), onClick = {}) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_forward_step),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

        }
        ////////////////////////////////////////////////


        Text(text = "isPlaying:${exoplayer.isPlaying.collectAsState().value}", color = Color.Yellow)
        //Text(text = "bufferedPercentage:${exoplayer.bufferedPercentage.collectAsState().value}", color = Color.Yellow)
        Text(text = ":${exoplayer.bitrate}", color = Color.Yellow)
        Text(text = ":${exoplayer.averageBitrate}", color = Color.Yellow)

        Text(text = "AudioOut:${AudioSampleRate.collectAsState().value} Hz", color = Color.Yellow)



        Mp3Route("R", audioMixerPump.routeR.collectAsState().value)
        Mp3Route("L", audioMixerPump.routeL.collectAsState().value)





    }

}

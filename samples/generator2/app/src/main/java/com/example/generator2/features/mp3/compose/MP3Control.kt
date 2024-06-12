package com.example.generator2.features.mp3.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.generator2.AppScreen
import com.example.generator2.R
import com.example.generator2.features.audio.AudioSampleRate
import com.example.generator2.features.mp3.formatMinSec
import com.example.generator2.screens.mainscreen4.VMMain4
import com.example.generator2.theme.Purple200
import com.example.generator2.theme.colorLightBackground


@Composable
fun MP3Control(vm: VMMain4) {

    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .background(colorLightBackground)
    ) {

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "P:${vm.audioMixerPump.exoplayer.currentTime.collectAsState().value.formatMinSec()}",
                color = Color.Yellow
            )
            Text(
                text = "D:${vm.audioMixerPump.exoplayer.durationMs.collectAsState().value.formatMinSec()}",
                color = Color.Yellow
            )
        }

        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = vm.audioMixerPump.exoplayer.currentTime.collectAsState().value.toFloat(),
            onValueChange = { timeMs: Float -> vm.audioMixerPump.exoplayer.player.seekTo(timeMs.toLong()) },
            valueRange = 0f..vm.audioMixerPump.exoplayer.durationMs.collectAsState().value.toFloat(),
            colors = SliderDefaults.colors(thumbColor = Purple200, activeTickColor = Purple200)
        )


        /////////////////////кнопки/////////////////////


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {


            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = {
                    navigator.push(AppScreen.Explorer)
                }) {
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

            IconButton(modifier = Modifier.size(40.dp), onClick = { vm.audioMixerPump.exoplayer.player.play() }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_play),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = { vm.audioMixerPump.exoplayer.player.pause() }) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(id = R.drawable.player_pause),
                    contentDescription = "Forward 10 seconds",
                    tint = Color.LightGray
                )
            }

            IconButton(modifier = Modifier.size(40.dp), onClick = { vm.audioMixerPump.exoplayer.player.stop() }) {
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

        //Text(text = "isPlaying:${exoplayer.isPlaying.collectAsState().value}", color = Color.Yellow)
        //Text(text = "bufferedPercentage:${exoplayer.bufferedPercentage.collectAsState().value}", color = Color.Yellow)
        //Text(text = ":${exoplayer.bitrate}", color = Color.Yellow)
        //Text(text = ":${exoplayer.averageBitrate}", color = Color.Yellow)

        Text(text = "AudioOut:${AudioSampleRate.collectAsState().value} Hz", color = Color.Yellow)


        Row(modifier = Modifier.fillMaxWidth()) {

            Column {
                Mp3Route("L", vm.audioMixerPump.routeL.collectAsState().value, vm.audioMixerPump)
                Mp3Route("R", vm.audioMixerPump.routeR.collectAsState().value, vm.audioMixerPump)
            }

            Icon(
                modifier = Modifier
                    .height(64.dp)
                    .width(32.dp)
                    //.offset((-1).dp)
                    .clickable(
                        onClick = {
                            vm.audioMixerPump.shuffle.value = vm.audioMixerPump.shuffle.value.not()
                        }
                    )
                    .border(1.dp, Color.Gray)
                    .padding(4.dp),
                painter = painterResource(id = R.drawable.shuffle74),
                contentDescription = "",
                tint = if (vm.audioMixerPump.shuffle.collectAsState().value) Color.Green else Color.DarkGray
            )

            //Стерео Моно
            Icon(
                modifier = Modifier
                    .height(64.dp)
                    .width(32.dp)
                    .offset((-1).dp)
                    .clickable(
                        onClick = { vm.audioMixerPump.gen.liveData.mono.value = !vm.audioMixerPump.gen.liveData.mono.value })
                    .border(1.dp, Color.Gray)
                    .padding(4.dp),
                painter = if (vm.audioMixerPump.gen.liveData.mono.collectAsState().value) painterResource(R.drawable.mono)
                else painterResource(R.drawable.stereo),
                contentDescription = null, tint = Color.LightGray
            )
        }


    }

}

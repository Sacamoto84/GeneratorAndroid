package com.example.generator2.screens.mainscreen4.top

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.features.scope.OSCILLSYNC
import com.example.generator2.screens.mainscreen4.VMMain4

@Suppress("NonSkippableComposable")
@Composable
fun TopBarAudioSource(vm: VMMain4) {

    val m = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .border(1.dp, Color.Gray)
        .background(Color.Magenta)

    Row(modifier = Modifier.fillMaxWidth()) {

        Box(
            modifier = m
                .weight(1f)
                .clickable(onClick = { }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MP3",
                color = if (vm.audioMixerPump.scope.oscillSync.value == OSCILLSYNC.NONE) Color.Green else Color.Gray
            )
        }

        Box(
            modifier = m
                .weight(1f)
                .clickable(onClick = { }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "GEN",
                color = if (vm.audioMixerPump.scope.oscillSync.value == OSCILLSYNC.NONE) Color.Green else Color.Gray
            )
        }

        Box(
            modifier = m
                .weight(1f)
                .clickable(onClick = { vm.audioMixerPump.scope.isUse.value = vm.audioMixerPump.scope.isUse.value.not() }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OSCILL",
                color = if (vm.audioMixerPump.scope.isUse.collectAsState().value) Color.Green else Color.Gray
            )
        }

    }
}
package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.screens.config.Config_header
import com.example.generator2.screens.config.DefScreenConfig
import com.example.generator2.screens.config.atom.editConfig
import com.example.generator2.screens.config.vm.VMConfig

@Composable
fun ConfigVolume(vm: VMConfig) {
    Config_header("Громкость")

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Максимальная громкость левый канал 0..1", color = Color.LightGray,
            maxLines = 3,
            minLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        val value0 = vm.gen.liveData.maxVolume0.collectAsState()
        editConfig(
            Modifier.width(DefScreenConfig.widthEdit).height(DefScreenConfig.heightEdit), "", value = value0, min = 0f, max = 1f,
            onDone = {
                vm.gen.liveData.maxVolume0.value = it
                vm.gen.liveData.volume0.value = vm.gen.liveData.currentVolume0.value * it
                vm.toastSaveVolume()
                vm.saveVolume()
            })
    }

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "Максимальная громкость правый канал 0..1", color = Color.LightGray,
            maxLines = 3,
            minLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        val value1 = vm.gen.liveData.maxVolume1.collectAsState()
        editConfig(
            Modifier.width(DefScreenConfig.widthEdit).height(DefScreenConfig.heightEdit), "", value = value1, min = 0f, max = 1f,
            onDone = {
                vm.gen.liveData.maxVolume1.value = it
                vm.gen.liveData.volume1.value = vm.gen.liveData.currentVolume1.value * it
                vm.toastSaveVolume()
                vm.saveVolume()
            })

    }
}
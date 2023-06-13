package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.generator2.model.LiveData
import com.example.generator2.screens.config.Config_header
import com.example.generator2.screens.config.atom.editConfig
import com.example.generator2.screens.config.vm.VMConfig

@Composable
fun ConfigVolume(vm: VMConfig)
{
    Config_header("Volume")

    Row(modifier = Modifier.fillMaxWidth()) {

        val value0 = LiveData.maxVolume0.collectAsState()
        editConfig(
            Modifier.weight(1f), "max Volume CH0 0..1", value = value0, min = 0f, max = 1f,
            onDone = {
                LiveData.maxVolume0.value = it
                LiveData.volume0.value = LiveData.currentVolume0.value * it
                vm.toastSaveVolume()
                vm.saveVolume()
            })

        val value1 = LiveData.maxVolume1.collectAsState()
        editConfig(
            Modifier.weight(1f), "max Volume CH1 0..1", value = value1, min = 0f, max = 1f,
            onDone = {
                LiveData.maxVolume1.value = it
                LiveData.volume1.value = LiveData.currentVolume1.value * it
                vm.toastSaveVolume()
                vm.saveVolume()
            } )

    }
}
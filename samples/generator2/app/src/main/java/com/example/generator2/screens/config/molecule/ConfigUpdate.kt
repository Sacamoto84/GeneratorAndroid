package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.generator2.MainRes
import com.example.generator2.features.update.ui.WidgetUpdate
import com.example.generator2.screens.config.ConfigGreenButton
import com.example.generator2.screens.config.atom.ConfigLineText
import com.example.generator2.screens.config.atom.ConfigLineTextSwitch
import com.example.generator2.screens.config.vm.VMConfig

@Composable
fun ConfigUpdate(vm: VMConfig) {
    Column {

        ConfigLineTextSwitch(MainRes.string.autoUpdate, vm.update.autoupdate.collectAsState().value,  { vm.update.autoupdate(it) } )
        ConfigLineText(MainRes.string.currentVersion, vm.update.currentVersion)
        ConfigLineText(MainRes.string.externalVersion, vm.update.externalVersion)

        ConfigGreenButton(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),{vm.update()},MainRes.string.update)

        WidgetUpdate(vm.update)
    }
}
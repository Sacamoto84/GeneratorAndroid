package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.MainRes
import com.example.generator2.features.update.ui.WidgetUpdate
import com.example.generator2.screens.config.DefScreenConfig
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

@Composable
fun ConfigGreenButton(
    modifier: Modifier, onClick: () -> Unit, label: String = ""
) {

    Button(
        modifier = modifier, content = {
            Text(
                text = label,
                color = Color.White,
                fontSize = DefScreenConfig.textSizeGreenButton
            )
        }, onClick = onClick, colors = ButtonDefaults.buttonColors(
            backgroundColor = DefScreenConfig.backgroundColorGreenButton,
            disabledBackgroundColor = DefScreenConfig.disabledBackgroundColorGreenButton
        )
    )

}
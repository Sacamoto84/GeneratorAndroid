package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.model.LiveConstrain
import com.example.generator2.screens.config.Config_header
import com.example.generator2.screens.config.atom.editConfig
import com.example.generator2.screens.config.vm.VMConfig


@Composable
private fun EditConfig(
    text: String,
    minCR: MutableFloatState,
    vm: VMConfig,
    toInt: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = text, color = Color.LightGray,
            maxLines = 3,
            minLines = 1,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )

        editConfig(
            Modifier.width(120.dp), "", value = minCR, min = 0f, max = 10000f, toInt = toInt,
            onDone = {
                minCR.floatValue = it
                vm.toastText("saved")
                vm.saveConstrain()
            })

    }
}


@Composable
fun ConfigConstrain(vm: VMConfig) {

    Config_header("Ограничение частот")

    EditConfig("Минимальная частота несущей", LiveConstrain.minCR, vm)
    EditConfig("Максимальная частота несущей", LiveConstrain.maxCR, vm)


    EditConfig("Минимальная частота FM девиации", LiveConstrain.minFMDev, vm)
    EditConfig("Максимальная частота FM девиации", LiveConstrain.maxFMDev, vm)

    EditConfig("Минимальная частота AM FM модуляции", LiveConstrain.minModAmFm, vm)
    EditConfig("Максимальная частота AM FM модуляции", LiveConstrain.maxModAmFm, vm)


    Divider()
    Config_header("Чувствительность слайдера")

    //Чувствительность слайдера
    //var sensetingSliderCr =  mutableStateOf( 0.2f)
    // var sensetingSliderFmDev =  mutableStateOf( 0.2f)
    // var sensetingSliderFmBase =  mutableStateOf( 0.2f)
    //  var sensetingSliderAmFm =  mutableStateOf( 0.01f)

    EditConfig(
        "Чувствительность слайдера несущая (0.2)",
        LiveConstrain.sensetingSliderCr,
        vm,
        toInt = false
    )
    EditConfig(
        "Чувствительность слайдера AM FM модуляции (0.01)",
        LiveConstrain.sensetingSliderAmFm,
        vm,
        toInt = false
    )
    EditConfig(
        "Чувствительность слайдера FM девиации (0.2)",
        LiveConstrain.sensetingSliderFmDev,
        vm,
        toInt = false
    )

}
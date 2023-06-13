package com.example.generator2.screens.config.molecule

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.generator2.model.LiveConstrain
import com.example.generator2.screens.config.Config_header
import com.example.generator2.screens.config.atom.editConfig
import com.example.generator2.screens.config.vm.VMConfig

@Composable
fun ConfigConstrain(vm: VMConfig)
{

    //CR min max
    Row(modifier = Modifier.fillMaxWidth()) {
        val minCR = LiveConstrain.minCR
        editConfig(
            Modifier.weight(1f), "min CR", value = minCR, min = 50f, max = 10000f, toInt = true,
            onDone = {
                LiveConstrain.minCR.value = it
                vm.toastText("min CR Saved")
                vm.saveConstrain()
            })

        val maxCR = LiveConstrain.maxCR
        editConfig(
            Modifier.weight(1f), "max CR", value = maxCR, min = 50f, max = 10000f, toInt = true,
            onDone = {
                LiveConstrain.maxCR.value = it
                vm.toastText("max CR Saved")
                vm.saveConstrain()
            })
    }

    //FMBase min max
    Row(modifier = Modifier.fillMaxWidth()) {
        val minFMBase = LiveConstrain.minFMBase
        editConfig(Modifier.weight(1f), "min FMBase", value = minFMBase, min = 50f, max = 10000f, toInt = true,
            onDone = {
                LiveConstrain.minFMBase.value = it
                vm.toastText("min FM Base Saved")
                vm.saveConstrain()
            })

        val maxFMBase = LiveConstrain.maxFMBase
        editConfig(Modifier.weight(1f), "max FMBase", value = maxFMBase, min = 50f, max = 10000f, toInt = true,
            onDone = {
                LiveConstrain.maxFMBase.value = it
                vm.toastText("max FM Base Saved")
                vm.saveConstrain()
            })
    }

    //FMDev min max
    Row(modifier = Modifier.fillMaxWidth()) {
        val minFMDev = LiveConstrain.minFMDev
        editConfig(Modifier.weight(1f), "min FMDev", value = minFMDev, min = 0f, max = 10000f, toInt = true,
            onDone = {
                LiveConstrain.minFMDev.value = it
                vm.toastText("min FM Dev Saved")
                vm.saveConstrain()
            })

        val maxFMDev = LiveConstrain.maxFMDev
        editConfig(Modifier.weight(1f), "max FMDev", value = maxFMDev, min = 0f, max = 10000f, toInt = true,
            onDone = {
                LiveConstrain.maxFMDev.value = it
                vm.toastText("max FM Dev Saved")
                vm.saveConstrain()
            })
    }

    //ModAmFm min max
    Row(modifier = Modifier.fillMaxWidth()) {
        val minModAmFm = LiveConstrain.minModAmFm
        editConfig(Modifier.weight(1f), "min Mod Am Fm", value = minModAmFm, min = 0f, max = 1000f, toInt = false,
            onDone = {
                LiveConstrain.minModAmFm.value = it
                vm.toastText("min Mod AmFm Saved")
                vm.saveConstrain()
            })

        val maxModAmFm = LiveConstrain.maxModAmFm
        editConfig(Modifier.weight(1f), "max Mod Am Fm", value = maxModAmFm, min = 0f, max = 1000f, toInt = false,
            onDone = {
                LiveConstrain.maxModAmFm.value = it
                vm.toastText("max Mod AmFm Saved")
                vm.saveConstrain()
            })
    }

    Divider()
    Config_header("Sensitivity Slider")

    //Чувствительность слайдера
    //var sensetingSliderCr =  mutableStateOf( 0.2f)
   // var sensetingSliderFmDev =  mutableStateOf( 0.2f)
   // var sensetingSliderFmBase =  mutableStateOf( 0.2f)
  //  var sensetingSliderAmFm =  mutableStateOf( 0.01f)

    Row(modifier = Modifier.fillMaxWidth()) {
        val sensetingSliderCr = LiveConstrain.sensetingSliderCr
        editConfig(Modifier.weight(1f),
            "Carrier 0.2",
            value = sensetingSliderCr,
            min = 0f,
            max = 1f,
            toInt = false,
            onDone = {
                LiveConstrain.sensetingSliderCr.value = it
                vm.toastText("sensitivity Slider Carrier Saved")
                vm.saveConstrain()
            })

        val sensetingSliderAmFm = LiveConstrain.sensetingSliderAmFm
        editConfig(Modifier.weight(1f),
            "Am Fm 0.01",
            value = sensetingSliderAmFm,
            min = 0f,
            max = 1f,
            toInt = false,
            onDone = {
                LiveConstrain.sensetingSliderAmFm.value = it
                vm.toastText("sensitivity Slider Am Fm Saved")
                vm.saveConstrain()
            })
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        val sensetingSliderFmBase = LiveConstrain.sensetingSliderFmBase
        editConfig(Modifier.weight(1f),
            "Fm Base 0.2",
            value = sensetingSliderFmBase,
            min = 0f,
            max = 1f,
            toInt = false,
            onDone = {
                LiveConstrain.sensetingSliderFmBase.value = it
                vm.toastText("sensitivity Slider Fm Base Saved")
                vm.saveConstrain()
            })

        val sensetingSliderFmDev = LiveConstrain.sensetingSliderFmDev
        editConfig(Modifier.weight(1f),
            "Fm Dev 0.2",
            value = sensetingSliderFmDev,
            min = 0f,
            max = 1f,
            toInt = false,
            onDone = {
                LiveConstrain.sensetingSliderFmDev.value = it
                vm.toastText("sensitivity Slider Fm Dev Saved")
                vm.saveConstrain()
            })
    }



}
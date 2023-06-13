package com.example.generator2.model

import androidx.compose.runtime.mutableFloatStateOf

object LiveConstrain {

    //Чувствительность слайдера
    var sensetingSliderCr =  mutableFloatStateOf( 0.2f)
    var sensetingSliderFmDev =  mutableFloatStateOf( 0.2f)
    var sensetingSliderFmBase =  mutableFloatStateOf( 0.2f)
    var sensetingSliderAmFm =  mutableFloatStateOf( 0.01f)

    var minCR =  mutableFloatStateOf( 600f )
    var maxCR =  mutableFloatStateOf(  4000f)

    var minModAmFm =  mutableFloatStateOf(  0.1f)
    var maxModAmFm =  mutableFloatStateOf(  100.0f)
    var minFMBase =  mutableFloatStateOf(  1000f)
    var maxFMBase =  mutableFloatStateOf(  3000f)
    var minFMDev =  mutableFloatStateOf(  1f)
    var maxFMDev =  mutableFloatStateOf(  2500f)

    var rangeSliderCr     = minCR.floatValue..maxCR.floatValue
    var rangeSliderAmFm   = minModAmFm.floatValue..maxModAmFm.floatValue
    var rangeSliderFmDev  = minFMDev.floatValue..maxFMDev.floatValue

}
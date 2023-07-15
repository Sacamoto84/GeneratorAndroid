package com.example.generator2.model

import androidx.compose.runtime.mutableFloatStateOf

object LiveConstrain {

    //Чувствительность слайдера
    var sensetingSliderCr =  mutableFloatStateOf( 0.2f)
    var sensetingSliderFmDev =  mutableFloatStateOf( 0.2f)
    var sensetingSliderAmFm =  mutableFloatStateOf( 0.01f)

}
package com.example.generator2.presets

import com.example.generator2.generator.DataLiveData
import com.example.generator2.generator.Generator

fun presetsToLiveData(data: DataLiveData, gen : Generator) {

    gen.liveData.presetsName.value = data.presetsName.value

    gen.liveData.ch1_EN.value = data.ch1_EN.value
    gen.liveData.ch1_Carrier_Filename.value = data.ch1_Carrier_Filename.value
    gen.liveData.ch1_Carrier_Fr.value = data.ch1_Carrier_Fr.value
    gen.liveData.ch1_AM_EN.value = data.ch1_AM_EN.value
    gen.liveData.ch1_AM_Filename.value = data.ch1_AM_Filename.value
    gen.liveData.ch1_AM_Fr.value = data.ch1_AM_Fr.value
    gen.liveData.ch1_FM_EN.value = data.ch1_FM_EN.value
    gen.liveData.ch1_FM_Filename.value = data.ch1_FM_Filename.value
    gen.liveData.ch1_FM_Dev.value = data.ch1_FM_Dev.value
    gen.liveData.ch1_FM_Fr.value = data.ch1_FM_Fr.value

    gen.liveData.ch2_EN.value = data.ch2_EN.value
    gen.liveData.ch2_Carrier_Filename.value = data.ch2_Carrier_Filename.value
    gen.liveData.ch2_Carrier_Fr.value = data.ch2_Carrier_Fr.value
    gen.liveData.ch2_AM_EN.value = data.ch2_AM_EN.value
    gen.liveData.ch2_AM_Filename.value = data.ch2_AM_Filename.value
    gen.liveData.ch2_AM_Fr.value = data.ch2_AM_Fr.value
    gen.liveData.ch2_FM_EN.value = data.ch2_FM_EN.value
    gen.liveData.ch2_FM_Filename.value = data.ch2_FM_Filename.value
    gen.liveData.ch2_FM_Dev.value = data.ch2_FM_Dev.value
    gen.liveData.ch2_FM_Fr.value = data.ch2_FM_Fr.value


    gen.liveData.volume0.value = data.volume0.value
    gen.liveData.volume1.value = data.volume1.value


    gen.liveData.mono.value = data.mono.value
    gen.liveData.invert.value = data.invert.value

    gen.liveData.shuffle.value = data.shuffle.value

    gen.liveData.enL.value = data.enL.value
    gen.liveData.enR.value = data.enR.value

    gen.liveData.maxVolume0.value = data.maxVolume0.value
    gen.liveData.maxVolume1.value = data.maxVolume1.value

    gen.liveData.currentVolume0.value = data.currentVolume0.value
    gen.liveData.currentVolume1.value = data.currentVolume1.value

    gen.liveData.ch1AmDepth.value = data.ch1AmDepth.value
    gen.liveData.ch2AmDepth.value = data.ch2AmDepth.value

    //Количество звезд
    gen.liveData.star.value = data.star.value

    gen.liveData.ch1FmMin.value =data.ch1FmMin.value
    gen.liveData.ch1FmMax.value =data.ch1FmMax.value
    gen.liveData.ch2FmMin.value =data.ch2FmMin.value
    gen.liveData.ch2FmMax.value =data.ch2FmMax.value
    //gen.liveData.parameterFloat4.value =data.parameterFloat4.value
    //gen.liveData.parameterFloat5.value =data.parameterFloat5.value
    //gen.liveData.parameterFloat6.value =data.parameterFloat6.value
    //gen.liveData.parameterFloat7.value =data.parameterFloat7.value

    gen.liveData.parameterInt0.value = data.parameterInt0.value
    gen.liveData.parameterInt1.value = data.parameterInt1.value
    //gen.liveData.parameterInt2.value = data.parameterInt2.value
    //gen.liveData.parameterInt3.value = data.parameterInt3.value
    //gen.liveData.parameterInt4.value = data.parameterInt4.value
    //gen.liveData.parameterInt5.value = data.parameterInt5.value
    //gen.liveData.parameterInt6.value = data.parameterInt6.value
    //gen.liveData.parameterInt7.value = data.parameterInt7.value

}
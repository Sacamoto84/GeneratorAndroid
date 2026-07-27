package com.example.generator2.features.presets

import com.example.generator2.features.generator.DataLiveData
import com.example.generator2.features.generator.Generator

//ВНИМАНИЕ: строковые ключи "ch1_*"/"ch2_*" — легаси-формат файлов пресетов.
//НЕ переименовывать в chL_/chR_: сломаются пресеты пользователей.

fun presetsToLiveData(data: DataLiveData, gen: Generator) {

    gen.liveData.presetsName.value = data.presetsName.value

    gen.liveData.chL_EN.value = data.chL_EN.value
    gen.liveData.chL_Carrier_Filename.value = data.chL_Carrier_Filename.value
    gen.liveData.chL_Carrier_Fr.value = data.chL_Carrier_Fr.value
    gen.liveData.chL_AM_EN.value = data.chL_AM_EN.value
    gen.liveData.chL_AM_Filename.value = data.chL_AM_Filename.value
    gen.liveData.chL_AM_Fr.value = data.chL_AM_Fr.value
    gen.liveData.chL_FM_EN.value = data.chL_FM_EN.value
    gen.liveData.chL_FM_Filename.value = data.chL_FM_Filename.value
    gen.liveData.chL_FM_Dev.value = data.chL_FM_Dev.value
    gen.liveData.chL_FM_Fr.value = data.chL_FM_Fr.value

    gen.liveData.chR_EN.value = data.chR_EN.value
    gen.liveData.chR_Carrier_Filename.value = data.chR_Carrier_Filename.value
    gen.liveData.chR_Carrier_Fr.value = data.chR_Carrier_Fr.value
    gen.liveData.chR_AM_EN.value = data.chR_AM_EN.value
    gen.liveData.chR_AM_Filename.value = data.chR_AM_Filename.value
    gen.liveData.chR_AM_Fr.value = data.chR_AM_Fr.value
    gen.liveData.chR_FM_EN.value = data.chR_FM_EN.value
    gen.liveData.chR_FM_Filename.value = data.chR_FM_Filename.value
    gen.liveData.chR_FM_Dev.value = data.chR_FM_Dev.value
    gen.liveData.chR_FM_Fr.value = data.chR_FM_Fr.value


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

    gen.liveData.chLAmDepth.value = data.chLAmDepth.value
    gen.liveData.chRAmDepth.value = data.chRAmDepth.value

    gen.liveData.chL_Master_EN.value = data.chL_Master_EN.value
    gen.liveData.chL_Master_Mode.value = data.chL_Master_Mode.value
    gen.liveData.chL_Master_Period.value = data.chL_Master_Period.value
    gen.liveData.chL_Master_Filename.value = data.chL_Master_Filename.value
    gen.liveData.chL_Master_TOn.value = data.chL_Master_TOn.value
    gen.liveData.chL_Master_TOff.value = data.chL_Master_TOff.value

    gen.liveData.chR_Master_EN.value = data.chR_Master_EN.value
    gen.liveData.chR_Master_Mode.value = data.chR_Master_Mode.value
    gen.liveData.chR_Master_Period.value = data.chR_Master_Period.value
    gen.liveData.chR_Master_Filename.value = data.chR_Master_Filename.value
    gen.liveData.chR_Master_TOn.value = data.chR_Master_TOn.value
    gen.liveData.chR_Master_TOff.value = data.chR_Master_TOff.value

    gen.liveData.chL_Morph_EN.value = data.chL_Morph_EN.value
    gen.liveData.chL_Morph_Mode.value = data.chL_Morph_Mode.value
    gen.liveData.chL_Morph_Time.value = data.chL_Morph_Time.value
    gen.liveData.chL_Morph_Slot0_EN.value = data.chL_Morph_Slot0_EN.value
    gen.liveData.chL_Morph_Slot1_EN.value = data.chL_Morph_Slot1_EN.value
    gen.liveData.chL_Morph_Slot2_EN.value = data.chL_Morph_Slot2_EN.value
    gen.liveData.chL_Morph_Slot0_Filename.value = data.chL_Morph_Slot0_Filename.value
    gen.liveData.chL_Morph_Slot1_Filename.value = data.chL_Morph_Slot1_Filename.value
    gen.liveData.chL_Morph_Slot2_Filename.value = data.chL_Morph_Slot2_Filename.value

    gen.liveData.chR_Morph_EN.value = data.chR_Morph_EN.value
    gen.liveData.chR_Morph_Mode.value = data.chR_Morph_Mode.value
    gen.liveData.chR_Morph_Time.value = data.chR_Morph_Time.value
    gen.liveData.chR_Morph_Slot0_EN.value = data.chR_Morph_Slot0_EN.value
    gen.liveData.chR_Morph_Slot1_EN.value = data.chR_Morph_Slot1_EN.value
    gen.liveData.chR_Morph_Slot2_EN.value = data.chR_Morph_Slot2_EN.value
    gen.liveData.chR_Morph_Slot0_Filename.value = data.chR_Morph_Slot0_Filename.value
    gen.liveData.chR_Morph_Slot1_Filename.value = data.chR_Morph_Slot1_Filename.value
    gen.liveData.chR_Morph_Slot2_Filename.value = data.chR_Morph_Slot2_Filename.value

    //Количество звезд
    gen.liveData.star.value = data.star.value

    gen.liveData.chLFmMin.value = data.chLFmMin.value
    gen.liveData.chLFmMax.value = data.chLFmMax.value
    gen.liveData.chRFmMin.value = data.chRFmMin.value
    gen.liveData.chRFmMax.value = data.chRFmMax.value
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
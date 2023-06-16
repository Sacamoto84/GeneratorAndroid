package com.example.generator2.presets

import com.example.generator2.model.LiveData
import com.example.generator2.model.dataLiveData

fun presetsToLiveData(data: dataLiveData) {

    LiveData.presetsName.value = data.presetsName.value

    LiveData.ch1_EN.value = data.ch1_EN.value
    LiveData.ch1_Carrier_Filename.value = data.ch1_Carrier_Filename.value
    LiveData.ch1_Carrier_Fr.value = data.ch1_Carrier_Fr.value
    LiveData.ch1_AM_EN.value = data.ch1_AM_EN.value
    LiveData.ch1_AM_Filename.value = data.ch1_AM_Filename.value
    LiveData.ch1_AM_Fr.value = data.ch1_AM_Fr.value
    LiveData.ch1_FM_EN.value = data.ch1_FM_EN.value
    LiveData.ch1_FM_Filename.value = data.ch1_FM_Filename.value
    LiveData.ch1_FM_Dev.value = data.ch1_FM_Dev.value
    LiveData.ch1_FM_Fr.value = data.ch1_FM_Fr.value

    LiveData.ch2_EN.value = data.ch2_EN.value
    LiveData.ch2_Carrier_Filename.value = data.ch2_Carrier_Filename.value
    LiveData.ch2_Carrier_Fr.value = data.ch2_Carrier_Fr.value
    LiveData.ch2_AM_EN.value = data.ch2_AM_EN.value
    LiveData.ch2_AM_Filename.value = data.ch2_AM_Filename.value
    LiveData.ch2_AM_Fr.value = data.ch2_AM_Fr.value
    LiveData.ch2_FM_EN.value = data.ch2_FM_EN.value
    LiveData.ch2_FM_Filename.value = data.ch2_FM_Filename.value
    LiveData.ch2_FM_Dev.value = data.ch2_FM_Dev.value
    LiveData.ch2_FM_Fr.value = data.ch2_FM_Fr.value


    LiveData.volume0.value = data.volume0.value
    LiveData.volume1.value = data.volume1.value


    LiveData.mono.value = data.mono.value
    LiveData. invert.value = data.invert.value

    LiveData.shuffle.value = data.shuffle.value

    LiveData.enL.value = data.enL.value
    LiveData.enR.value = data.enR.value

    LiveData.maxVolume0.value = data.maxVolume0.value
    LiveData.maxVolume1.value = data.maxVolume1.value

    LiveData.currentVolume0.value = data.currentVolume0.value
    LiveData.currentVolume1.value = data.currentVolume1.value

    LiveData.ch1AmDepth.value = data.ch1AmDepth.value
    LiveData.ch2AmDepth.value = data.ch2AmDepth.value
    /**
     *  ### Импульсный режим
     */
    LiveData.impulse0.value = data.impulse0.value
    LiveData.impulse1.value = data.impulse1.value

    // Ширина импульса в тиках
    LiveData.impulse0timeImp.value = data.impulse0timeImp.value
    LiveData.impulse1timeImp.value = data.impulse1timeImp.value

    // Пауза в тиках
    LiveData.impulse0timeImpPause.value = data.impulse0timeImpPause.value
    LiveData.impulse1timeImpPause.value = data.impulse1timeImpPause.value

    //Количество звезд
    LiveData.star.value = data.star.value












}
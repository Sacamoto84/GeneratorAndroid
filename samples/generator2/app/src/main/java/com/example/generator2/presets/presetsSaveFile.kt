package com.example.generator2.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.AppPath
import com.example.generator2.model.LiveData
import java.io.File

/**
 * Создание пресета по имени
 */
fun presetsSaveFile(name: String) {

    val satchel =
        Satchel.with(
            storer = FileSatchelStorer(File(AppPath().presets, "${name}.txt")),
            encrypter = BypassSatchelEncrypter,
            serializer = RawSatchelSerializer
        )

    //Количество звезд в перссете, для сортировки
    satchel["star"] = LiveData.star.value

    LiveData.presetsName.value = name
    satchel["presetsName"] = name

    satchel["ch1_EN"] = LiveData.ch1_EN.value
    satchel["ch1_Carrier_Filename"] = LiveData.ch1_Carrier_Filename.value
    satchel["ch1_Carrier_Fr"] = LiveData.ch1_Carrier_Fr.value    //Частота несущей
    satchel["ch1_AM_EN"] = LiveData.ch1_AM_EN.value
    satchel["ch1_AM_Filename"] = LiveData.ch1_AM_Filename.value
    satchel["ch1_AM_Fr"] = LiveData.ch1_AM_Fr.value
    satchel["ch1_FM_EN"] = LiveData.ch1_FM_EN.value
    satchel["ch1_FM_Filename"] = LiveData.ch1_FM_Filename.value
    satchel["ch1_FM_Dev"] = LiveData.ch1_FM_Dev.value      //Частота базы
    satchel["ch1_FM_Fr"] = LiveData.ch1_FM_Fr.value

    satchel["ch2_EN"] = LiveData.ch2_EN.value
    satchel["ch2_Carrier_Filename"] = LiveData.ch2_Carrier_Filename.value
    satchel["ch2_Carrier_Fr"] = LiveData.ch2_Carrier_Fr.value //Частота несущей
    satchel["ch2_AM_EN"] = LiveData.ch2_AM_EN.value
    satchel["ch2_AM_Filename"] = LiveData.ch2_AM_Filename.value
    satchel["ch2_AM_Fr"] = LiveData.ch2_AM_Fr.value
    satchel["ch2_FM_EN"] = LiveData.ch2_FM_EN.value
    satchel["ch2_FM_Filename"] = LiveData.ch2_FM_Filename.value
    satchel["ch2_FM_Dev"] = LiveData.ch2_FM_Dev.value //Частота базы
    satchel["ch2_FM_Fr"] = LiveData.ch2_FM_Fr.value

    //Используется для AudioDevice = maxVolume0 * currentVolume0
    satchel["volume0"] = LiveData.volume0.value
    satchel["volume1"] = LiveData.volume1.value

    satchel["mono"] = LiveData.mono.value //Режим повторения настроек второго канала с первым
    satchel["invert"] = LiveData.invert.value //Инверсия сигнала во втором канале, только при моно
    satchel["shuffle"] = LiveData.shuffle.value //меняем левый и правый канал в стерео режиме

    satchel["enL"] = LiveData.enL.value
    satchel["enR"] = LiveData.enR.value

    //JsonVolume максимальная громкость усилителя
    satchel["maxVolume0"] = LiveData.maxVolume0.value
    satchel["maxVolume1"] = LiveData.maxVolume1.value

    //Громкость канала на регуляторе 0 100 JsonConfig()
    satchel["currentVolume0"] = LiveData.currentVolume0.value
    satchel["currentVolume1"] = LiveData.currentVolume1.value

    satchel["ch1AmDepth"] = LiveData.ch1AmDepth.value  //Глубина AM модуляции
    satchel["ch2AmDepth"] = LiveData.ch2AmDepth.value  //Глубина AM модуляции

    /**
     *  ### Импульсный режим
     */

    satchel["impulse0"] = LiveData.impulse0.value    //Импульсный режим канала
    satchel["impulse1"] = LiveData.impulse1.value

    // Ширина импульса в тиках
    satchel["impulse0timeImp"] = LiveData.impulse0timeImp.value
    satchel["impulse1timeImp"] = LiveData.impulse1timeImp.value

    // Пауза в тиках
    satchel["impulse0timeImpPause"] = LiveData.impulse0timeImpPause.value
    satchel["impulse1timeImpPause"] = LiveData.impulse1timeImpPause.value



}
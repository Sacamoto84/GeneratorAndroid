package com.example.generator2.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.AppPath
import com.example.generator2.model.dataLiveData
import java.io.File

fun presetsReadFile(name: String): dataLiveData {

    val satchel =
        Satchel.with(
            storer = FileSatchelStorer(File(AppPath().presets, "${name}.txt")),
            encrypter = BypassSatchelEncrypter,
            serializer = RawSatchelSerializer
        )

    val data = dataLiveData()

    data.presetsName.value = name

    //Количество звезд в перссете, для сортировки
    data.star.value = satchel.getOrDefault("star", 0)

    data.ch1_EN.value = satchel.getOrDefault("ch1_EN", false)
    data.ch1_Carrier_Filename.value = satchel.getOrDefault("ch1_Carrier_Filename", "Sine")
    data.ch1_Carrier_Fr.value = satchel.getOrDefault("ch1_Carrier_Fr", 400.0f)   //Частота несущей
    data.ch1_AM_EN.value = satchel.getOrDefault("ch1_AM_EN", false)
    data.ch1_AM_Filename.value = satchel.getOrDefault("ch1_AM_Filename", "Sine")
    data.ch1_AM_Fr.value = satchel.getOrDefault("ch1_AM_Fr", 8.7f)
    data.ch1_FM_EN.value = satchel.getOrDefault("ch1_FM_EN", false)
    data.ch1_FM_Filename.value = satchel.getOrDefault("ch1_FM_Filename", "Sine")
    data.ch1_FM_Dev.value = satchel.getOrDefault("ch1_FM_Dev", 1100f)       //Частота базы
    data.ch1_FM_Fr.value = satchel.getOrDefault("ch1_FM_Fr", 5.1f)

    data.ch2_EN.value = satchel.getOrDefault("ch2_EN", false)
    data.ch2_Carrier_Filename.value = satchel.getOrDefault("ch2_Carrier_Filename", "Sine")
    data.ch2_Carrier_Fr.value = satchel.getOrDefault("ch2_Carrier_Fr", 2000.0f) //Частота несущей
    data.ch2_AM_EN.value = satchel.getOrDefault("ch2_AM_EN", false)
    data.ch2_AM_Filename.value = satchel.getOrDefault("ch2_AM_Filename", "Sine")
    data.ch2_AM_Fr.value = satchel.getOrDefault("ch2_AM_Fr", 8.7f)
    data.ch2_FM_EN.value = satchel.getOrDefault("ch2_FM_EN", false)
    data.ch2_FM_Filename.value = satchel.getOrDefault("ch2_FM_Filename", "Sine")
    data.ch2_FM_Dev.value = satchel.getOrDefault("ch2_FM_Dev", 1100f) //Частота базы
    data.ch2_FM_Fr.value = satchel.getOrDefault("ch2_FM_Fr", 5.1f)

    //Используется для AudioDevice = maxVolume0 * currentVolume0
    data.volume0.value = satchel.getOrDefault("volume0", 1f)
    data.volume1.value = satchel.getOrDefault("volume1", 1f)

    data.mono.value =
        satchel.getOrDefault("mono", false) //Режим повторения настроек второго канала с первым
    data.invert.value =
        satchel.getOrDefault("invert", false) //Инверсия сигнала во втором канале, только при моно

    data.shuffle.value =
        satchel.getOrDefault("shuffle", false)//меняем левый и правый канал в стерео режиме

    data.enL.value = satchel.getOrDefault("enL", true)
    data.enR.value = satchel.getOrDefault("enR", true)

    //JsonVolume максимальная громкость усилителя
    data.maxVolume0.value = satchel.getOrDefault("maxVolume0", 0.9f)
    data.maxVolume1.value = satchel.getOrDefault("maxVolume1", 0.9f)

    //Громкость канала на регуляторе 0 100 JsonConfig()
    data.currentVolume0.value = satchel.getOrDefault("currentVolume0", 1f)
    data.currentVolume1.value = satchel.getOrDefault("currentVolume1", 1f)

    data.ch1AmDepth.value = satchel.getOrDefault("ch1AmDepth", 1f)  //Глубина AM модуляции
    data.ch2AmDepth.value = satchel.getOrDefault("ch2AmDepth", 1f)  //Глубина AM модуляции

    /**
     *  ### Импульсный режим
     */

    data.impulse0.value = satchel.getOrDefault("impulse0", false) //Импульсный режим канала
    data.impulse1.value = satchel.getOrDefault("impulse1", false)

    // Ширина импульса в тиках
    data.impulse0timeImp.value = satchel.getOrDefault("impulse0timeImp", 9)
    data.impulse1timeImp.value = satchel.getOrDefault("impulse1timeImp", 9)

    // Пауза в тиках
    data.impulse0timeImpPause.value = satchel.getOrDefault("impulse0timeImpPause", 9)
    data.impulse1timeImpPause.value = satchel.getOrDefault("impulse1timeImpPause", 9)



    return data
}
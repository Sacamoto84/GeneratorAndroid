package com.example.generator2.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.AppPath
import com.example.generator2.generator.DataLiveData
import java.io.File

fun presetsReadFile(name: String, path : String = AppPath().presets ): DataLiveData {

    val satchel =
        Satchel.with(
            storer = FileSatchelStorer(File(path, "${name}.txt")),
            encrypter = BypassSatchelEncrypter,
            serializer = RawSatchelSerializer
        )

    val data = DataLiveData()

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

    //Используется для AudioDevice = maxVolume0 * currentVolume0
    data.volume0.value = satchel.getOrDefault("volume0", 1f)
    data.volume1.value = satchel.getOrDefault("volume1", 1f)

    data.ch1AmDepth.value = satchel.getOrDefault("ch1AmDepth", 1f)  //Глубина AM модуляции
    data.ch2AmDepth.value = satchel.getOrDefault("ch2AmDepth", 1f)  //Глубина AM модуляции

    data.parameterFloat0.value = satchel.getOrDefault("parameterFloat0", 1000.0F) //CH1 FM min
    data.parameterFloat1.value = satchel.getOrDefault("parameterFloat1", 2000.0F) //CH1 FM max
    data.parameterFloat2.value = satchel.getOrDefault("parameterFloat2", 1000.0F) //CH2 FM min
    data.parameterFloat3.value = satchel.getOrDefault("parameterFloat3", 2000.0F) //CH2 FM max

    if (data.parameterFloat0.value < 10f) data.parameterFloat0.value = 1000f
    if (data.parameterFloat1.value < 10f) data.parameterFloat1.value = 2000f
    if (data.parameterFloat2.value < 10f) data.parameterFloat2.value = 1000f
    if (data.parameterFloat3.value < 10f) data.parameterFloat3.value = 2000f

    data.parameterFloat4.value = satchel.getOrDefault("parameterFloat4", 0.0F)
    data.parameterFloat5.value = satchel.getOrDefault("parameterFloat5", 0.0F)
    data.parameterFloat6.value = satchel.getOrDefault("parameterFloat6", 0.0F)
    data.parameterFloat7.value = satchel.getOrDefault("parameterFloat7", 0.0F)

    data.parameterInt0.value = satchel.getOrDefault("parameterInt0", 0)
    data.parameterInt1.value = satchel.getOrDefault("parameterInt1", 0)
    data.parameterInt2.value = satchel.getOrDefault("parameterInt2", 0)
    data.parameterInt3.value = satchel.getOrDefault("parameterInt3", 0)
    data.parameterInt4.value = satchel.getOrDefault("parameterInt4", 0)
    data.parameterInt5.value = satchel.getOrDefault("parameterInt5", 0)
    data.parameterInt6.value = satchel.getOrDefault("parameterInt6", 0)
    data.parameterInt7.value = satchel.getOrDefault("parameterInt7", 0)


    return data
}
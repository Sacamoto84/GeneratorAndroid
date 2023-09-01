package com.example.generator2.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.AppPath
import com.example.generator2.gen
import com.example.generator2.util.toast
import java.io.File

/**
 * Создание пресета по имени
 */
fun presetsSaveFile(name: String, path: String = AppPath().presets) {

    val satchel =
        Satchel.with(
            storer = FileSatchelStorer(File(path, "${name}.txt")),
            encrypter = BypassSatchelEncrypter,
            serializer = RawSatchelSerializer
        )

    //Количество звезд в перссете, для сортировки
    satchel["star"] = gen.liveData.star.value

    if (name != "default")
        gen.liveData.presetsName.value = name

    satchel["presetsName"] = name

    satchel["ch1_EN"] = gen.liveData.ch1_EN.value
    satchel["ch1_Carrier_Filename"] = gen.liveData.ch1_Carrier_Filename.value
    satchel["ch1_Carrier_Fr"] = gen.liveData.ch1_Carrier_Fr.value    //Частота несущей
    satchel["ch1_AM_EN"] = gen.liveData.ch1_AM_EN.value
    satchel["ch1_AM_Filename"] = gen.liveData.ch1_AM_Filename.value
    satchel["ch1_AM_Fr"] = gen.liveData.ch1_AM_Fr.value
    satchel["ch1_FM_EN"] = gen.liveData.ch1_FM_EN.value
    satchel["ch1_FM_Filename"] = gen.liveData.ch1_FM_Filename.value
    satchel["ch1_FM_Dev"] = gen.liveData.ch1_FM_Dev.value      //Частота базы
    satchel["ch1_FM_Fr"] = gen.liveData.ch1_FM_Fr.value

    satchel["ch2_EN"] = gen.liveData.ch2_EN.value
    satchel["ch2_Carrier_Filename"] = gen.liveData.ch2_Carrier_Filename.value
    satchel["ch2_Carrier_Fr"] = gen.liveData.ch2_Carrier_Fr.value //Частота несущей
    satchel["ch2_AM_EN"] = gen.liveData.ch2_AM_EN.value
    satchel["ch2_AM_Filename"] = gen.liveData.ch2_AM_Filename.value
    satchel["ch2_AM_Fr"] = gen.liveData.ch2_AM_Fr.value
    satchel["ch2_FM_EN"] = gen.liveData.ch2_FM_EN.value
    satchel["ch2_FM_Filename"] = gen.liveData.ch2_FM_Filename.value
    satchel["ch2_FM_Dev"] = gen.liveData.ch2_FM_Dev.value //Частота базы
    satchel["ch2_FM_Fr"] = gen.liveData.ch2_FM_Fr.value

    satchel["mono"] = gen.liveData.mono.value //Режим повторения настроек второго канала с первым
    satchel["invert"] = gen.liveData.invert.value //Инверсия сигнала во втором канале, только при моно
    satchel["shuffle"] = gen.liveData.shuffle.value //меняем левый и правый канал в стерео режиме

    satchel["enL"] = gen.liveData.enL.value
    satchel["enR"] = gen.liveData.enR.value

    //JsonVolume максимальная громкость усилителя
    satchel["maxVolume0"] = gen.liveData.maxVolume0.value
    satchel["maxVolume1"] = gen.liveData.maxVolume1.value

    //Громкость канала на регуляторе 0 100 JsonConfig()
    satchel["currentVolume0"] = gen.liveData.currentVolume0.value
    satchel["currentVolume1"] = gen.liveData.currentVolume1.value

    //Используется для AudioDevice = maxVolume0 * currentVolume0
    satchel["volume0"] = gen.liveData.volume0.value
    satchel["volume1"] = gen.liveData.volume1.value

    satchel["ch1AmDepth"] = gen.liveData.ch1AmDepth.value  //Глубина AM модуляции
    satchel["ch2AmDepth"] = gen.liveData.ch2AmDepth.value  //Глубина AM модуляции

    satchel["parameterFloat0"] = gen.liveData.parameterFloat0.value
    satchel["parameterFloat1"] = gen.liveData.parameterFloat1.value
    satchel["parameterFloat2"] = gen.liveData.parameterFloat2.value
    satchel["parameterFloat3"] = gen.liveData.parameterFloat3.value
    satchel["parameterFloat4"] = gen.liveData.parameterFloat4.value
    satchel["parameterFloat5"] = gen.liveData.parameterFloat5.value
    satchel["parameterFloat6"] = gen.liveData.parameterFloat6.value
    satchel["parameterFloat7"] = gen.liveData.parameterFloat7.value

    satchel["parameterInt0"] = gen.liveData.parameterInt0.value
    satchel["parameterInt1"] = gen.liveData.parameterInt1.value
    satchel["parameterInt2"] = gen.liveData.parameterInt2.value
    satchel["parameterInt3"] = gen.liveData.parameterInt3.value
    satchel["parameterInt4"] = gen.liveData.parameterInt4.value
    satchel["parameterInt5"] = gen.liveData.parameterInt5.value
    satchel["parameterInt6"] = gen.liveData.parameterInt6.value
    satchel["parameterInt7"] = gen.liveData.parameterInt7.value

    if (name != "default")
        toast.show("Пресет $name сохранен")

}
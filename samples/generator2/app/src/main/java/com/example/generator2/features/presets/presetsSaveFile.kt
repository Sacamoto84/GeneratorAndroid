package com.example.generator2.features.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.features.generator.Generator
import java.io.File

//ВНИМАНИЕ: строковые ключи "ch1_*"/"ch2_*" — легаси-формат файлов пресетов.
//НЕ переименовывать в chL_/chR_: сломаются пресеты пользователей.

/**
 * Создание пресета по имени
 */
fun presetsSaveFile(name: String, path: String, gen: Generator): String {

    var result : String = ""

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

    satchel["ch1_EN"] = gen.liveData.chL_EN.value
    satchel["ch1_Carrier_Filename"] = gen.liveData.chL_Carrier_Filename.value
    satchel["ch1_Carrier_Fr"] = gen.liveData.chL_Carrier_Fr.value    //Частота несущей
    satchel["ch1_AM_EN"] = gen.liveData.chL_AM_EN.value
    satchel["ch1_AM_Filename"] = gen.liveData.chL_AM_Filename.value
    satchel["ch1_AM_Fr"] = gen.liveData.chL_AM_Fr.value
    satchel["ch1_FM_EN"] = gen.liveData.chL_FM_EN.value
    satchel["ch1_FM_Filename"] = gen.liveData.chL_FM_Filename.value
    satchel["ch1_FM_Dev"] = gen.liveData.chL_FM_Dev.value      //Частота базы
    satchel["ch1_FM_Fr"] = gen.liveData.chL_FM_Fr.value

    satchel["ch2_EN"] = gen.liveData.chR_EN.value
    satchel["ch2_Carrier_Filename"] = gen.liveData.chR_Carrier_Filename.value
    satchel["ch2_Carrier_Fr"] = gen.liveData.chR_Carrier_Fr.value //Частота несущей
    satchel["ch2_AM_EN"] = gen.liveData.chR_AM_EN.value
    satchel["ch2_AM_Filename"] = gen.liveData.chR_AM_Filename.value
    satchel["ch2_AM_Fr"] = gen.liveData.chR_AM_Fr.value
    satchel["ch2_FM_EN"] = gen.liveData.chR_FM_EN.value
    satchel["ch2_FM_Filename"] = gen.liveData.chR_FM_Filename.value
    satchel["ch2_FM_Dev"] = gen.liveData.chR_FM_Dev.value //Частота базы
    satchel["ch2_FM_Fr"] = gen.liveData.chR_FM_Fr.value

    satchel["mono"] = gen.liveData.mono.value //Режим повторения настроек второго канала с первым
    satchel["invert"] =
        gen.liveData.invert.value //Инверсия сигнала во втором канале, только при моно
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

    satchel["ch1AmDepth"] = gen.liveData.chLAmDepth.value  //Глубина AM модуляции
    satchel["ch2AmDepth"] = gen.liveData.chRAmDepth.value  //Глубина AM модуляции

    satchel["ch1_Master_EN"] = gen.liveData.chL_Master_EN.value
    satchel["ch1_Master_Mode"] = gen.liveData.chL_Master_Mode.value
    satchel["ch1_Master_Period"] = gen.liveData.chL_Master_Period.value
    satchel["ch1_Master_Filename"] = gen.liveData.chL_Master_Filename.value
    satchel["ch1_Master_TOn"] = gen.liveData.chL_Master_TOn.value
    satchel["ch1_Master_TOff"] = gen.liveData.chL_Master_TOff.value

    satchel["ch2_Master_EN"] = gen.liveData.chR_Master_EN.value
    satchel["ch2_Master_Mode"] = gen.liveData.chR_Master_Mode.value
    satchel["ch2_Master_Period"] = gen.liveData.chR_Master_Period.value
    satchel["ch2_Master_Filename"] = gen.liveData.chR_Master_Filename.value
    satchel["ch2_Master_TOn"] = gen.liveData.chR_Master_TOn.value
    satchel["ch2_Master_TOff"] = gen.liveData.chR_Master_TOff.value

    satchel["ch1_Morph_EN"] = gen.liveData.chL_Morph_EN.value
    satchel["ch1_Morph_Mode"] = gen.liveData.chL_Morph_Mode.value
    satchel["ch1_Morph_Time"] = gen.liveData.chL_Morph_Time.value
    satchel["ch1_Morph_Slot0_EN"] = gen.liveData.chL_Morph_Slot0_EN.value
    satchel["ch1_Morph_Slot1_EN"] = gen.liveData.chL_Morph_Slot1_EN.value
    satchel["ch1_Morph_Slot2_EN"] = gen.liveData.chL_Morph_Slot2_EN.value
    satchel["ch1_Morph_Slot0_Filename"] = gen.liveData.chL_Morph_Slot0_Filename.value
    satchel["ch1_Morph_Slot1_Filename"] = gen.liveData.chL_Morph_Slot1_Filename.value
    satchel["ch1_Morph_Slot2_Filename"] = gen.liveData.chL_Morph_Slot2_Filename.value

    satchel["ch2_Morph_EN"] = gen.liveData.chR_Morph_EN.value
    satchel["ch2_Morph_Mode"] = gen.liveData.chR_Morph_Mode.value
    satchel["ch2_Morph_Time"] = gen.liveData.chR_Morph_Time.value
    satchel["ch2_Morph_Slot0_EN"] = gen.liveData.chR_Morph_Slot0_EN.value
    satchel["ch2_Morph_Slot1_EN"] = gen.liveData.chR_Morph_Slot1_EN.value
    satchel["ch2_Morph_Slot2_EN"] = gen.liveData.chR_Morph_Slot2_EN.value
    satchel["ch2_Morph_Slot0_Filename"] = gen.liveData.chR_Morph_Slot0_Filename.value
    satchel["ch2_Morph_Slot1_Filename"] = gen.liveData.chR_Morph_Slot1_Filename.value
    satchel["ch2_Morph_Slot2_Filename"] = gen.liveData.chR_Morph_Slot2_Filename.value

    satchel["ch1FmMin"] = gen.liveData.chLFmMin.value
    satchel["ch1FmMax"] = gen.liveData.chLFmMax.value
    satchel["ch2FmMin"] = gen.liveData.chRFmMin.value
    satchel["ch2FmMax"] = gen.liveData.chRFmMax.value
    //satchel["parameterFloat4"] = gen.liveData.parameterFloat4.value
    //satchel["parameterFloat5"] = gen.liveData.parameterFloat5.value
    //satchel["parameterFloat6"] = gen.liveData.parameterFloat6.value
    //satchel["parameterFloat7"] = gen.liveData.parameterFloat7.value

    satchel["parameterInt0"] = gen.liveData.parameterInt0.value
    satchel["parameterInt1"] = gen.liveData.parameterInt1.value
    //satchel["parameterInt2"] = gen.liveData.parameterInt2.value
    //satchel["parameterInt3"] = gen.liveData.parameterInt3.value
    //satchel["parameterInt4"] = gen.liveData.parameterInt4.value
    //satchel["parameterInt5"] = gen.liveData.parameterInt5.value
    //satchel["parameterInt6"] = gen.liveData.parameterInt6.value
    //satchel["parameterInt7"] = gen.liveData.parameterInt7.value

    if (name != "default")
        result = "Пресет $name сохранен"

    return result
}
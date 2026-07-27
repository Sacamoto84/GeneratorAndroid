package com.example.generator2.features.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.features.generator.DataLiveData
import java.io.File

//ВНИМАНИЕ: строковые ключи "ch1_*"/"ch2_*" — легаси-формат файлов пресетов.
//НЕ переименовывать в chL_/chR_: сломаются пресеты пользователей.

fun presetsReadFile(name: String, path: String): DataLiveData {

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

    data.chL_EN.value = satchel.getOrDefault("ch1_EN", false)
    data.chL_Carrier_Filename.value = satchel.getOrDefault("ch1_Carrier_Filename", "Sine")
    data.chL_Carrier_Fr.value = satchel.getOrDefault("ch1_Carrier_Fr", 400.0f)   //Частота несущей
    data.chL_AM_EN.value = satchel.getOrDefault("ch1_AM_EN", false)
    data.chL_AM_Filename.value = satchel.getOrDefault("ch1_AM_Filename", "01_SINE_12b")
    data.chL_AM_Fr.value = satchel.getOrDefault("ch1_AM_Fr", 8.7f)
    data.chL_FM_EN.value = satchel.getOrDefault("ch1_FM_EN", false)
    data.chL_FM_Filename.value = satchel.getOrDefault("ch1_FM_Filename", "01_SINE_12b")
    data.chL_FM_Dev.value = satchel.getOrDefault("ch1_FM_Dev", 1100f)       //Частота базы
    data.chL_FM_Fr.value = satchel.getOrDefault("ch1_FM_Fr", 5.1f)

    data.chR_EN.value = satchel.getOrDefault("ch2_EN", false)
    data.chR_Carrier_Filename.value = satchel.getOrDefault("ch2_Carrier_Filename", "Sine")
    data.chR_Carrier_Fr.value = satchel.getOrDefault("ch2_Carrier_Fr", 2000.0f) //Частота несущей
    data.chR_AM_EN.value = satchel.getOrDefault("ch2_AM_EN", false)
    data.chR_AM_Filename.value = satchel.getOrDefault("ch2_AM_Filename", "01_SINE_12b")
    data.chR_AM_Fr.value = satchel.getOrDefault("ch2_AM_Fr", 8.7f)
    data.chR_FM_EN.value = satchel.getOrDefault("ch2_FM_EN", false)
    data.chR_FM_Filename.value = satchel.getOrDefault("ch2_FM_Filename", "01_SINE_12b")
    data.chR_FM_Dev.value = satchel.getOrDefault("ch2_FM_Dev", 1100f) //Частота базы
    data.chR_FM_Fr.value = satchel.getOrDefault("ch2_FM_Fr", 5.1f)

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

    data.chLAmDepth.value = satchel.getOrDefault("ch1AmDepth", 1f)  //Глубина AM модуляции
    data.chRAmDepth.value = satchel.getOrDefault("ch2AmDepth", 1f)  //Глубина AM модуляции

    data.chL_Master_EN.value = satchel.getOrDefault("ch1_Master_EN", false)
    data.chL_Master_Mode.value = satchel.getOrDefault("ch1_Master_Mode", 1)
    data.chL_Master_Period.value = satchel.getOrDefault("ch1_Master_Period", 2f)
    data.chL_Master_Filename.value = satchel.getOrDefault("ch1_Master_Filename", "09_Ramp")
    data.chL_Master_TOn.value = satchel.getOrDefault("ch1_Master_TOn", 1f)
    data.chL_Master_TOff.value = satchel.getOrDefault("ch1_Master_TOff", 1f)

    data.chR_Master_EN.value = satchel.getOrDefault("ch2_Master_EN", false)
    data.chR_Master_Mode.value = satchel.getOrDefault("ch2_Master_Mode", 1)
    data.chR_Master_Period.value = satchel.getOrDefault("ch2_Master_Period", 2f)
    data.chR_Master_Filename.value = satchel.getOrDefault("ch2_Master_Filename", "09_Ramp")
    data.chR_Master_TOn.value = satchel.getOrDefault("ch2_Master_TOn", 1f)
    data.chR_Master_TOff.value = satchel.getOrDefault("ch2_Master_TOff", 1f)

    data.chL_Morph_EN.value = satchel.getOrDefault("ch1_Morph_EN", false)
    data.chL_Morph_Mode.value = satchel.getOrDefault("ch1_Morph_Mode", 1)
    data.chL_Morph_Time.value = satchel.getOrDefault("ch1_Morph_Time", 2f)
    data.chL_Morph_Slot0_EN.value = satchel.getOrDefault("ch1_Morph_Slot0_EN", true)
    data.chL_Morph_Slot1_EN.value = satchel.getOrDefault("ch1_Morph_Slot1_EN", true)
    data.chL_Morph_Slot2_EN.value = satchel.getOrDefault("ch1_Morph_Slot2_EN", false)
    data.chL_Morph_Slot0_Filename.value = satchel.getOrDefault("ch1_Morph_Slot0_Filename", "Sine")
    data.chL_Morph_Slot1_Filename.value = satchel.getOrDefault("ch1_Morph_Slot1_Filename", "Square")
    data.chL_Morph_Slot2_Filename.value = satchel.getOrDefault("ch1_Morph_Slot2_Filename", "Ramp")

    data.chR_Morph_EN.value = satchel.getOrDefault("ch2_Morph_EN", false)
    data.chR_Morph_Mode.value = satchel.getOrDefault("ch2_Morph_Mode", 1)
    data.chR_Morph_Time.value = satchel.getOrDefault("ch2_Morph_Time", 2f)
    data.chR_Morph_Slot0_EN.value = satchel.getOrDefault("ch2_Morph_Slot0_EN", true)
    data.chR_Morph_Slot1_EN.value = satchel.getOrDefault("ch2_Morph_Slot1_EN", true)
    data.chR_Morph_Slot2_EN.value = satchel.getOrDefault("ch2_Morph_Slot2_EN", false)
    data.chR_Morph_Slot0_Filename.value = satchel.getOrDefault("ch2_Morph_Slot0_Filename", "Sine")
    data.chR_Morph_Slot1_Filename.value = satchel.getOrDefault("ch2_Morph_Slot1_Filename", "Square")
    data.chR_Morph_Slot2_Filename.value = satchel.getOrDefault("ch2_Morph_Slot2_Filename", "Ramp")

    data.chLFmMin.value = satchel.getOrDefault("ch1FmMin", 1000.0F) //CH1 FM min
    data.chLFmMax.value = satchel.getOrDefault("ch1FmMax", 2000.0F) //CH1 FM max
    data.chRFmMin.value = satchel.getOrDefault("parameterFloat2", 1000.0F) //CH2 FM min
    data.chRFmMax.value = satchel.getOrDefault("parameterFloat3", 2000.0F) //CH2 FM max

    if (data.chLFmMin.value < 10f) data.chLFmMin.value = 1000f
    if (data.chLFmMax.value < 10f) data.chLFmMax.value = 2000f
    if (data.chRFmMin.value < 10f) data.chRFmMin.value = 1000f
    if (data.chRFmMax.value < 10f) data.chRFmMax.value = 2000f

    //data.parameterFloat4.value = satchel.getOrDefault("parameterFloat4", 0.0F)
    //data.parameterFloat5.value = satchel.getOrDefault("parameterFloat5", 0.0F)
    //data.parameterFloat6.value = satchel.getOrDefault("parameterFloat6", 0.0F)
    //data.parameterFloat7.value = satchel.getOrDefault("parameterFloat7", 0.0F)

    data.parameterInt0.value = satchel.getOrDefault("parameterInt0", 0)
    data.parameterInt1.value = satchel.getOrDefault("parameterInt1", 0)
    //data.parameterInt2.value = satchel.getOrDefault("parameterInt2", 0)
    //data.parameterInt3.value = satchel.getOrDefault("parameterInt3", 0)
    //data.parameterInt4.value = satchel.getOrDefault("parameterInt4", 0)
    //data.parameterInt5.value = satchel.getOrDefault("parameterInt5", 0)
    //data.parameterInt6.value = satchel.getOrDefault("parameterInt6", 0)
    //data.parameterInt7.value = satchel.getOrDefault("parameterInt7", 0)


    return data
}
package com.example.generator2.features.presets

import com.example.generator2.features.generator.DataLiveData
import com.example.generator2.features.storage.KvFile
import com.example.generator2.features.storage.valueOr
import java.io.File

//ВНИМАНИЕ: строковые ключи "ch1_*"/"ch2_*" — легаси-формат файлов пресетов.
//НЕ переименовывать в chL_/chR_: сломаются пресеты пользователей.

/**
 * Чтение пресета. Битый файл не роняет вызывающего: подхватывается `.bak`, а
 * если и его нет — поля берут значения по умолчанию.
 */
fun presetsReadFile(name: String, path: String): DataLiveData {

    val values = KvFile.read(File(path, "$name.txt"))

    val data = DataLiveData()

    data.presetsName.value = name

    //Количество звезд в перссете, для сортировки
    data.star.value = values.valueOr("star", 0)

    data.chL_EN.value = values.valueOr("ch1_EN", false)
    data.chL_Carrier_Filename.value = values.valueOr("ch1_Carrier_Filename", "Sine")
    data.chL_Carrier_Fr.value = values.valueOr("ch1_Carrier_Fr", 400.0f)   //Частота несущей
    data.chL_AM_EN.value = values.valueOr("ch1_AM_EN", false)
    data.chL_AM_Filename.value = values.valueOr("ch1_AM_Filename", "01_SINE_12b")
    data.chL_AM_Fr.value = values.valueOr("ch1_AM_Fr", 8.7f)
    data.chL_FM_EN.value = values.valueOr("ch1_FM_EN", false)
    data.chL_FM_Filename.value = values.valueOr("ch1_FM_Filename", "01_SINE_12b")
    data.chL_FM_Dev.value = values.valueOr("ch1_FM_Dev", 1100f)       //Частота базы
    data.chL_FM_Fr.value = values.valueOr("ch1_FM_Fr", 5.1f)

    data.chR_EN.value = values.valueOr("ch2_EN", false)
    data.chR_Carrier_Filename.value = values.valueOr("ch2_Carrier_Filename", "Sine")
    data.chR_Carrier_Fr.value = values.valueOr("ch2_Carrier_Fr", 2000.0f) //Частота несущей
    data.chR_AM_EN.value = values.valueOr("ch2_AM_EN", false)
    data.chR_AM_Filename.value = values.valueOr("ch2_AM_Filename", "01_SINE_12b")
    data.chR_AM_Fr.value = values.valueOr("ch2_AM_Fr", 8.7f)
    data.chR_FM_EN.value = values.valueOr("ch2_FM_EN", false)
    data.chR_FM_Filename.value = values.valueOr("ch2_FM_Filename", "01_SINE_12b")
    data.chR_FM_Dev.value = values.valueOr("ch2_FM_Dev", 1100f) //Частота базы
    data.chR_FM_Fr.value = values.valueOr("ch2_FM_Fr", 5.1f)

    //Режим повторения настроек второго канала с первым
    data.mono.value = values.valueOr("mono", false)
    //Инверсия сигнала во втором канале, только при моно
    data.invert.value = values.valueOr("invert", false)
    //меняем левый и правый канал в стерео режиме
    data.shuffle.value = values.valueOr("shuffle", false)

    data.enL.value = values.valueOr("enL", true)
    data.enR.value = values.valueOr("enR", true)

    //JsonVolume максимальная громкость усилителя
    data.maxVolume0.value = values.valueOr("maxVolume0", 0.9f)
    data.maxVolume1.value = values.valueOr("maxVolume1", 0.9f)

    //Громкость канала на регуляторе 0 100 JsonConfig()
    data.currentVolume0.value = values.valueOr("currentVolume0", 1f)
    data.currentVolume1.value = values.valueOr("currentVolume1", 1f)

    //Используется для AudioDevice = maxVolume0 * currentVolume0
    data.volume0.value = values.valueOr("volume0", 1f)
    data.volume1.value = values.valueOr("volume1", 1f)

    data.chLAmDepth.value = values.valueOr("ch1AmDepth", 1f)  //Глубина AM модуляции
    data.chRAmDepth.value = values.valueOr("ch2AmDepth", 1f)  //Глубина AM модуляции

    data.chL_Master_EN.value = values.valueOr("ch1_Master_EN", false)
    data.chL_Master_Mode.value = values.valueOr("ch1_Master_Mode", 1)
    data.chL_Master_Period.value = values.valueOr("ch1_Master_Period", 2f)
    data.chL_Master_Filename.value = values.valueOr("ch1_Master_Filename", "09_Ramp")
    data.chL_Master_TOn.value = values.valueOr("ch1_Master_TOn", 1f)
    data.chL_Master_TOff.value = values.valueOr("ch1_Master_TOff", 1f)

    data.chR_Master_EN.value = values.valueOr("ch2_Master_EN", false)
    data.chR_Master_Mode.value = values.valueOr("ch2_Master_Mode", 1)
    data.chR_Master_Period.value = values.valueOr("ch2_Master_Period", 2f)
    data.chR_Master_Filename.value = values.valueOr("ch2_Master_Filename", "09_Ramp")
    data.chR_Master_TOn.value = values.valueOr("ch2_Master_TOn", 1f)
    data.chR_Master_TOff.value = values.valueOr("ch2_Master_TOff", 1f)

    data.chL_Morph_EN.value = values.valueOr("ch1_Morph_EN", false)
    data.chL_Morph_Mode.value = values.valueOr("ch1_Morph_Mode", 1)
    data.chL_Morph_Time.value = values.valueOr("ch1_Morph_Time", 2f)
    data.chL_Morph_Slot0_EN.value = values.valueOr("ch1_Morph_Slot0_EN", true)
    data.chL_Morph_Slot1_EN.value = values.valueOr("ch1_Morph_Slot1_EN", true)
    data.chL_Morph_Slot2_EN.value = values.valueOr("ch1_Morph_Slot2_EN", false)
    data.chL_Morph_Slot0_Filename.value = values.valueOr("ch1_Morph_Slot0_Filename", "Sine")
    data.chL_Morph_Slot1_Filename.value = values.valueOr("ch1_Morph_Slot1_Filename", "Square")
    data.chL_Morph_Slot2_Filename.value = values.valueOr("ch1_Morph_Slot2_Filename", "Ramp")

    data.chR_Morph_EN.value = values.valueOr("ch2_Morph_EN", false)
    data.chR_Morph_Mode.value = values.valueOr("ch2_Morph_Mode", 1)
    data.chR_Morph_Time.value = values.valueOr("ch2_Morph_Time", 2f)
    data.chR_Morph_Slot0_EN.value = values.valueOr("ch2_Morph_Slot0_EN", true)
    data.chR_Morph_Slot1_EN.value = values.valueOr("ch2_Morph_Slot1_EN", true)
    data.chR_Morph_Slot2_EN.value = values.valueOr("ch2_Morph_Slot2_EN", false)
    data.chR_Morph_Slot0_Filename.value = values.valueOr("ch2_Morph_Slot0_Filename", "Sine")
    data.chR_Morph_Slot1_Filename.value = values.valueOr("ch2_Morph_Slot1_Filename", "Square")
    data.chR_Morph_Slot2_Filename.value = values.valueOr("ch2_Morph_Slot2_Filename", "Ramp")

    data.chLFmMin.value = values.valueOr("ch1FmMin", 1000.0F) //CH1 FM min
    data.chLFmMax.value = values.valueOr("ch1FmMax", 2000.0F) //CH1 FM max
    //Сохранялось всегда в ch2FmMin/ch2FmMax, а читалось из parameterFloat2/3 —
    //границы FM второго канала не восстанавливались. Старый ключ оставлен
    //запасным ради файлов, записанных до появления ch2FmMin/ch2FmMax
    data.chRFmMin.value = values.valueOr("ch2FmMin", values.valueOr("parameterFloat2", 1000.0F))
    data.chRFmMax.value = values.valueOr("ch2FmMax", values.valueOr("parameterFloat3", 2000.0F))

    if (data.chLFmMin.value < 10f) data.chLFmMin.value = 1000f
    if (data.chLFmMax.value < 10f) data.chLFmMax.value = 2000f
    if (data.chRFmMin.value < 10f) data.chRFmMin.value = 1000f
    if (data.chRFmMax.value < 10f) data.chRFmMax.value = 2000f

    data.parameterInt0.value = values.valueOr("parameterInt0", 0)
    data.parameterInt1.value = values.valueOr("parameterInt1", 0)

    return data
}

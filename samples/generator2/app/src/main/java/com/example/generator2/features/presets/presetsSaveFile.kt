package com.example.generator2.features.presets

import com.example.generator2.features.generator.Generator
import com.example.generator2.features.storage.KvFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

//ВНИМАНИЕ: строковые ключи "ch1_*"/"ch2_*" — легаси-формат файлов пресетов.
//НЕ переименовывать в chL_/chR_: сломаются пресеты пользователей.

/**
 * Сохранение из обработчика UI: снимок состояния берётся сразу и на вызывающем
 * потоке, файл пишется на IO — блокировать кадры записью на sdcard нельзя.
 *
 * Запись помечена NonCancellable: уход с экрана не должен обрывать сохранение
 * на середине.
 *
 * @param onSaved вызывается на потоке [scope] после записи, туда приходит текст
 * результата — момент, когда файл уже на диске и список пресетов можно обновлять
 */
fun presetsSaveInBackground(
    name: String,
    path: String,
    gen: Generator,
    scope: CoroutineScope,
    onSaved: (result: String) -> Unit = {},
) {
    if (name != "default")
        gen.liveData.presetsName.value = name

    val snapshot = presetsSnapshot(name, gen)

    scope.launch {
        val result = withContext(Dispatchers.IO + NonCancellable) {
            presetsWrite(name, path, snapshot)
        }
        onSaved(result)
    }
}

/**
 * Запись готового снимка на диск. Атомарна: обрыв процесса не оставит
 * обрезанный пресет, прошлая версия остаётся в `<имя>.txt.bak`.
 */
fun presetsWrite(name: String, path: String, snapshot: Map<String, Any>): String =
    try {
        KvFile.write(File(path, "$name.txt"), snapshot)
        if (name != "default") "Пресет $name сохранен" else ""
    } catch (e: Exception) {
        Timber.e(e, "Не удалось сохранить пресет $name")
        "Не удалось сохранить пресет $name"
    }

/**
 * Снимок текущего состояния генератора. Только чтение полей, без ввода-вывода —
 * безопасно звать с главного потока.
 */
fun presetsSnapshot(name: String, gen: Generator): Map<String, Any> {

    val d = gen.liveData
    val values = LinkedHashMap<String, Any>(96)

    //Количество звезд в перссете, для сортировки
    values["star"] = d.star.value

    values["presetsName"] = name

    values["ch1_EN"] = d.chL_EN.value
    values["ch1_Carrier_Filename"] = d.chL_Carrier_Filename.value
    values["ch1_Carrier_Fr"] = d.chL_Carrier_Fr.value    //Частота несущей
    values["ch1_AM_EN"] = d.chL_AM_EN.value
    values["ch1_AM_Filename"] = d.chL_AM_Filename.value
    values["ch1_AM_Fr"] = d.chL_AM_Fr.value
    values["ch1_FM_EN"] = d.chL_FM_EN.value
    values["ch1_FM_Filename"] = d.chL_FM_Filename.value
    values["ch1_FM_Dev"] = d.chL_FM_Dev.value      //Частота базы
    values["ch1_FM_Fr"] = d.chL_FM_Fr.value

    values["ch2_EN"] = d.chR_EN.value
    values["ch2_Carrier_Filename"] = d.chR_Carrier_Filename.value
    values["ch2_Carrier_Fr"] = d.chR_Carrier_Fr.value //Частота несущей
    values["ch2_AM_EN"] = d.chR_AM_EN.value
    values["ch2_AM_Filename"] = d.chR_AM_Filename.value
    values["ch2_AM_Fr"] = d.chR_AM_Fr.value
    values["ch2_FM_EN"] = d.chR_FM_EN.value
    values["ch2_FM_Filename"] = d.chR_FM_Filename.value
    values["ch2_FM_Dev"] = d.chR_FM_Dev.value //Частота базы
    values["ch2_FM_Fr"] = d.chR_FM_Fr.value

    values["mono"] = d.mono.value       //Режим повторения настроек второго канала с первым
    values["invert"] = d.invert.value   //Инверсия сигнала во втором канале, только при моно
    values["shuffle"] = d.shuffle.value //меняем левый и правый канал в стерео режиме

    values["enL"] = d.enL.value
    values["enR"] = d.enR.value

    //JsonVolume максимальная громкость усилителя
    values["maxVolume0"] = d.maxVolume0.value
    values["maxVolume1"] = d.maxVolume1.value

    //Громкость канала на регуляторе 0 100 JsonConfig()
    values["currentVolume0"] = d.currentVolume0.value
    values["currentVolume1"] = d.currentVolume1.value

    //Используется для AudioDevice = maxVolume0 * currentVolume0
    values["volume0"] = d.volume0.value
    values["volume1"] = d.volume1.value

    values["ch1AmDepth"] = d.chLAmDepth.value  //Глубина AM модуляции
    values["ch2AmDepth"] = d.chRAmDepth.value  //Глубина AM модуляции

    values["ch1_Master_EN"] = d.chL_Master_EN.value
    values["ch1_Master_Mode"] = d.chL_Master_Mode.value
    values["ch1_Master_Period"] = d.chL_Master_Period.value
    values["ch1_Master_Filename"] = d.chL_Master_Filename.value
    values["ch1_Master_TOn"] = d.chL_Master_TOn.value
    values["ch1_Master_TOff"] = d.chL_Master_TOff.value

    values["ch2_Master_EN"] = d.chR_Master_EN.value
    values["ch2_Master_Mode"] = d.chR_Master_Mode.value
    values["ch2_Master_Period"] = d.chR_Master_Period.value
    values["ch2_Master_Filename"] = d.chR_Master_Filename.value
    values["ch2_Master_TOn"] = d.chR_Master_TOn.value
    values["ch2_Master_TOff"] = d.chR_Master_TOff.value

    values["ch1_Morph_EN"] = d.chL_Morph_EN.value
    values["ch1_Morph_Mode"] = d.chL_Morph_Mode.value
    values["ch1_Morph_Time"] = d.chL_Morph_Time.value
    values["ch1_Morph_Slot0_EN"] = d.chL_Morph_Slot0_EN.value
    values["ch1_Morph_Slot1_EN"] = d.chL_Morph_Slot1_EN.value
    values["ch1_Morph_Slot2_EN"] = d.chL_Morph_Slot2_EN.value
    values["ch1_Morph_Slot0_Filename"] = d.chL_Morph_Slot0_Filename.value
    values["ch1_Morph_Slot1_Filename"] = d.chL_Morph_Slot1_Filename.value
    values["ch1_Morph_Slot2_Filename"] = d.chL_Morph_Slot2_Filename.value

    values["ch2_Morph_EN"] = d.chR_Morph_EN.value
    values["ch2_Morph_Mode"] = d.chR_Morph_Mode.value
    values["ch2_Morph_Time"] = d.chR_Morph_Time.value
    values["ch2_Morph_Slot0_EN"] = d.chR_Morph_Slot0_EN.value
    values["ch2_Morph_Slot1_EN"] = d.chR_Morph_Slot1_EN.value
    values["ch2_Morph_Slot2_EN"] = d.chR_Morph_Slot2_EN.value
    values["ch2_Morph_Slot0_Filename"] = d.chR_Morph_Slot0_Filename.value
    values["ch2_Morph_Slot1_Filename"] = d.chR_Morph_Slot1_Filename.value
    values["ch2_Morph_Slot2_Filename"] = d.chR_Morph_Slot2_Filename.value

    values["ch1FmMin"] = d.chLFmMin.value
    values["ch1FmMax"] = d.chLFmMax.value
    values["ch2FmMin"] = d.chRFmMin.value
    values["ch2FmMax"] = d.chRFmMax.value

    values["parameterInt0"] = d.parameterInt0.value
    values["parameterInt1"] = d.parameterInt1.value

    return values
}

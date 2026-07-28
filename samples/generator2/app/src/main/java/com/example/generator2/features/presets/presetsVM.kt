package com.example.generator2.features.presets

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.generator2.AppPath
import com.example.generator2.common.snackbar.SnackBar
import com.example.generator2.features.generator.Generator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


class presetsVM @Inject constructor(
    val gen: Generator,
    val appPath: AppPath
) : ScreenModel {

    /**
     * Чтение пресета по клику. Экран закрывает вызывающая сторона.
     *
     * Файл читается на IO, применение к liveData — на главном потоке.
     */
    fun onClickPresetsRead(name: String) {
        screenModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) { presetsReadFile(name, appPath.presets) }
                presetsToLiveData(data, gen)
                SnackBar.success("Пресет «$name» применён")
            } catch (e: Exception) {
                Timber.e(e, "onClickPresetsRead($name)")
                SnackBar.error("Не удалось применить «$name»")
            }
        }
    }

    /**
     * Сохранение пресета под именем [name].
     *
     * Живёт в scope экрана, а не диалога: диалог закрывается сразу после нажатия,
     * а [onSaved] должен дождаться реальной записи файла.
     */
    fun savePreset(name: String, onSaved: () -> Unit = {}) {
        presetsSaveInBackground(name, appPath.presets, gen, screenModelScope) { onSaved() }
    }
}
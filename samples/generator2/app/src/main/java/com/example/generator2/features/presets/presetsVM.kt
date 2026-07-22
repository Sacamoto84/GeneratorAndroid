package com.example.generator2.features.presets

import cafe.adriel.voyager.core.model.ScreenModel
import com.example.generator2.AppPath
import com.example.generator2.common.snackbar.SnackBar
import com.example.generator2.features.generator.Generator
import timber.log.Timber
import javax.inject.Inject


class presetsVM @Inject constructor(
    val gen: Generator,
    val appPath: AppPath
) : ScreenModel {

    /**
     * Чтение пресета по клику. Экран закрывает вызывающая сторона.
     */
    fun onClickPresetsRead(name: String) {
        try {
            presetsToLiveData(presetsReadFile(name, appPath.presets), gen)
            SnackBar.success("Пресет «$name» применён")
        } catch (e: Exception) {
            Timber.e(e, "onClickPresetsRead($name)")
            SnackBar.error("Не удалось применить «$name»")
        }
    }


}
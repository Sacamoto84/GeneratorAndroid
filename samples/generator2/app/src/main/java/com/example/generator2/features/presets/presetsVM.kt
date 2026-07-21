package com.example.generator2.features.presets

import cafe.adriel.voyager.core.model.ScreenModel
import com.example.generator2.AppPath
import com.example.generator2.features.generator.Generator
import javax.inject.Inject


class presetsVM @Inject constructor(
    val gen: Generator,
    val appPath: AppPath
) : ScreenModel {

    /**
     * Чтение пресета по клику
     */
    fun onClickPresetsRead(name: String) {
        presetsToLiveData(presetsReadFile(name, appPath.presets), gen)
        //navController.popBackStack()
    }


}
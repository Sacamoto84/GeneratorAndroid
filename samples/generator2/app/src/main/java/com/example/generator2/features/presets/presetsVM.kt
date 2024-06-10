package com.example.generator2.features.presets

import androidx.lifecycle.ViewModel
import com.example.generator2.AppPath
import com.example.generator2.features.generator.Generator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class presetsVM @Inject constructor(
    val gen: Generator,
    val appPath: AppPath
) : ViewModel() {

    /**
     * Чтение пресета по клику
     */
    fun onClickPresetsRead(name: String) {
        presetsToLiveData(presetsReadFile(name, appPath.presets), gen)
        //navController.popBackStack()
    }


}
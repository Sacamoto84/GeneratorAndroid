package com.example.generator2.presets

import androidx.lifecycle.ViewModel
import com.example.generator2.generator.Generator
import com.example.generator2.navController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class presetsVM @Inject constructor(
    val gen : Generator
) : ViewModel() {

    /**
     * Чтение пресета по клику
     */
    fun onClickPresetsRead(name : String) {
        presetsToLiveData(presetsReadFile(name), gen)
        navController.popBackStack()
    }


}
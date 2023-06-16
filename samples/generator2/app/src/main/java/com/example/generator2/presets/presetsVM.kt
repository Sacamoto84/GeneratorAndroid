package com.example.generator2.presets

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.generator2.di.Hub
import com.example.generator2.model.LiveData
import com.example.generator2.navController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@HiltViewModel
class presetsVM @Inject constructor(
    @ApplicationContext contextActivity: Context,
    val hub: Hub
) : ViewModel() {


    /**
     * Чтение пресета по клику
     */
    fun onClickPresetsRead(name : String) {

        val l = presetsReadFile(name)
        LiveData = l.copy()

        hub.audioDevice.sendAlltoGen()

        navController.popBackStack()
    }


}
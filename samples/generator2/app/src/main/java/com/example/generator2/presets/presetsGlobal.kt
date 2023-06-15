package com.example.generator2.presets

import kotlinx.coroutines.flow.MutableStateFlow


object Presets{

    //Текущий список для отображения и сохранения, да и вообще работы
    var presetList = mutableListOf<String>()

    //Открыть диалог
    var isOpenDialog = MutableStateFlow(true)



}
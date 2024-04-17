package com.example.generator2.features.presets

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.flow.MutableStateFlow


object Presets {

    //Текущий список для отображения и сохранения, да и вообще работы
    var presetList = mutableStateListOf<String>() //mutableListOf<String>()

    //Открыть диалог создания нового пресета
    var isOpenDialogNewFile = MutableStateFlow(false)

    //Открыть диалог создания нового пресета
    var isOpenDialogDeleteRename = MutableStateFlow(false)

    //Открыть диалог создания нового пресета
    var isOpenDialogRename = MutableStateFlow(false)

    //Открыть диалог создания нового пресета
    var isOpenDialogDelete = MutableStateFlow(false)

    //Открыть диалог создания нового пресета
    var isOpenDialogDeleteRenameName = ""

}
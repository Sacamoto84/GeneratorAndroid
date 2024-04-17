package com.example.generator2.features.presets

import com.example.generator2.AppPath

fun presetsInit(appPath: AppPath) {
    //Текущий список файлов читаемый из папки
    Presets.presetList = presetsGetListName(appPath)
}
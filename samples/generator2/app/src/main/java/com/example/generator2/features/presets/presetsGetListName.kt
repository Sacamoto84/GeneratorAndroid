package com.example.generator2.features.presets

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.example.generator2.AppPath

fun presetsGetListName(appPath: AppPath): SnapshotStateList<String> {
    val list = presetsGetListFile(appPath.presets).sorted()
    val l2 = list.map { it.absolutePath.substringAfterLast('/').substringBeforeLast('.') }

    return l2.toMutableStateList()
}
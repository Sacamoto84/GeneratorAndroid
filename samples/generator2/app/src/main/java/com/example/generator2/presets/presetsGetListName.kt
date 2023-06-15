package com.example.generator2.presets

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

fun presetsGetListName(): SnapshotStateList<String> {
    val list = presetsGetListFile().sorted()
    val l2 = list.map { it.absolutePath.substringAfterLast('/').substringBeforeLast('.') }

    return l2.toMutableStateList()
}
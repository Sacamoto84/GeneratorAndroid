package com.example.generator2.presets

fun presetsGetListName(): List<String> {
    val list = presetsGetListFile()
    return list.map { it.absolutePath.substringAfterLast('/').substringBeforeLast('.') }
}
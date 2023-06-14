package com.example.generator2.presets

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import com.example.generator2.AppPath
import java.io.File


/**
 * Прочитать текущий список с диска
 */
fun presetsReadList():List<String> {

    val satchel =
        Satchel.with(
            storer = FileSatchelStorer(File(AppPath().presets, "list.txt")),
            encrypter = BypassSatchelEncrypter,
            serializer = RawSatchelSerializer
        )

    return satchel.getOrDefault("list", emptyList() )

}
package com.example.generator2.features.noSQL

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.SatchelStorage
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import java.io.File

//= AppPath().config
class NoSQL(val path: String, val nameDB: String) {

    val satchel: SatchelStorage = Satchel.with(
        storer = FileSatchelStorer(File(path, "${nameDB}.db")),
        encrypter = BypassSatchelEncrypter,
        serializer = RawSatchelSerializer
    )

    inline fun <reified T : Any> read(key: String, default: T): T {
        val res: T = satchel.getOrDefault(key, default)
        //Timber.i("NoSQL read key:$key value:$res name:$nameDB path:$path")
        return res
    }

    inline fun <reified T : Any> write(key: String, value: T) = run { satchel[key] = value }

    fun remove(key: String) = satchel.remove(key)

    fun clear() = satchel.clear()

    /**
     * Получить список всех ключей
     */
    fun keys(): Set<String> = satchel.keys


}
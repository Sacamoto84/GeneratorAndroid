package com.example.generator2

import android.os.Environment
import java.io.File

enum class Folder(val value: String) {
    CARRIER("Carrier"),
    MOD("Mod"),
    SCRIPT("Script"),
    CONFIG("Config"),
    DOWNLOAD("Download"),
    PRESETS("Presets"),
    MUSIC("Music")
}

class AppPath {

    private val appMain = "Gen3"

    val main      = Environment.getExternalStorageDirectory().absolutePath + "/" + appMain
    val carrier   = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.CARRIER.value}"
    val mod       = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.MOD.value}"
    val script    = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.SCRIPT.value}"
    val config    = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.CONFIG.value}"
    val download  = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.DOWNLOAD.value}"
    val presets   = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.PRESETS.value}"
    val music     = Environment.getExternalStorageDirectory().absolutePath + "/${appMain}/${Folder.MUSIC.value}"

    fun mkDir() {
        File(main).mkdirs()
        File(carrier).mkdirs()
        File(mod).mkdirs()
        File(script).mkdirs()
        File(config).mkdirs()
        File(download).mkdirs()
        File(presets).mkdirs()
        File(music).mkdirs()
    }

}

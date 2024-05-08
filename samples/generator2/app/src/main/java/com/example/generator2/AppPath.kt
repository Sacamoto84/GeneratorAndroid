package com.example.generator2

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.ContextCompat.startActivity
import java.io.File

private enum class Folder(val value: String) {
    CARRIER("Carrier"),
    MOD("Mod"),
    SCRIPT("Script"),
    CONFIG("Config"),
    DOWNLOAD("Download"),
    PRESETS("Presets"),
    MUSIC("Music"),
    PLAYLIST("Playlist"),
}

enum class EnvironmentStorage {
    INTERNAL,
    EXTERNAL,
    EXTERNAL_STORAGE,
}

/**
 * Предоставляет пути хранения файлов
 */
class AppPath(
    context: Context,
    val root: EnvironmentStorage = EnvironmentStorage.EXTERNAL_STORAGE
) {

    private val appMain = "Gen3"

    private val envoriment = when (root) {
        EnvironmentStorage.INTERNAL -> Environment.getDataDirectory() // /data
        EnvironmentStorage.EXTERNAL -> (context as Application).getExternalFilesDir(null) // /storage/sdcard0/Android/data/package/files
        EnvironmentStorage.EXTERNAL_STORAGE -> Environment.getExternalStorageDirectory() // /storage/sdcard0
    }





    /**
     * Путь до sdcard
     */
    val sdcard = Environment.getExternalStorageDirectory().absolutePath.toString()


    val main = envoriment?.absolutePath + "/" + appMain

    val carrier = envoriment?.absolutePath  + "/${appMain}/${Folder.CARRIER.value}"
    val mod = envoriment?.absolutePath  + "/${appMain}/${Folder.MOD.value}"
    val script = envoriment?.absolutePath + "/${appMain}/${Folder.SCRIPT.value}"
    val config = envoriment?.absolutePath + "/${appMain}/${Folder.CONFIG.value}"
    val download = envoriment?.absolutePath + "/${appMain}/${Folder.DOWNLOAD.value}"
    val presets = envoriment?.absolutePath + "/${appMain}/${Folder.PRESETS.value}"
    val music = envoriment?.absolutePath + "/${appMain}/${Folder.MUSIC.value}"
    val playlist = envoriment?.absolutePath + "/${appMain}/${Folder.PLAYLIST.value}"

    val assets = "file:///android_asset/"

    init {
        println("---AppPath---")
        println("sdcard: $sdcard")

        File(main).mkdirs()
        File(carrier).mkdirs()
        File(mod).mkdirs()
        File(script).mkdirs()
        File(config).mkdirs()
        File(download).mkdirs()
        File(presets).mkdirs()
        File(music).mkdirs()
        File(playlist).mkdirs()
    }

}

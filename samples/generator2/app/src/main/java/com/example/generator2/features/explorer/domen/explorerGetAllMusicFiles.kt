package com.example.generator2.features.explorer.domen

import android.content.Context
import android.provider.MediaStore
import timber.log.Timber

/**
 * ## Функция для получения списка всех музыкальных файлов, возвращает список путей к файлам
 */
fun explorerGetAllMusicFiles(context: Context): List<String> {

    val musicList = mutableListOf<String>()

    try {

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATA),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )

        cursor?.use {
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                // val id = it.getLong(idColumn)
                val data = it.getString(dataColumn)
                // Делайте что-то с этими данными, например, сохраняйте путь к файлу в список
                musicList.add(data)
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Timber.e(e)
    }

    return musicList
}








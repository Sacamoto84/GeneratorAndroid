package com.example.generator2.features.explorer.domen

import android.content.Context
import android.provider.MediaStore
import timber.log.Timber

/**
 * ## Функция для получения списка всех музыкальных файлов, возвращает список путей к файлам
 */
fun explorerGetAllMusicFiles(context: Context): List<String> {

    val musicList = mutableListOf<String>()

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

    val projection = arrayOf(
//        MediaStore.Audio.Media._ID,
//        MediaStore.Audio.Media.TITLE,
//        MediaStore.Audio.Media.ARTIST,
//        MediaStore.Audio.Media.ALBUM,
//        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
//        MediaStore.Audio.Media.SIZE,
//        MediaStore.Audio.Media.NUM_TRACKS,
//        MediaStore.Audio.Media.BITRATE,
        )

    try {


        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )



        cursor?.use {
            //val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            //val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            //val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            //val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            //val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            //val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            //val numTracksColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.NUM_TRACKS)
            //val bitrateColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)

            while (it.moveToNext()) {
                // val id = it.getLong(idColumn)
                // val title = it.getString(titleColumn)
                // val artist = it.getString(artistColumn)
                // val album = it.getString(albumColumn)
                // val duration = it.getInt(durationColumn)
                val data = it.getString(dataColumn)
                // val size = it.getLong(sizeColumn)
                // val numTracks = it.getInt(numTracksColumn)
                // val bitrate = it.getInt(bitrateColumn)

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








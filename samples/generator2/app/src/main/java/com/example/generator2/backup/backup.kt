package com.example.generator2.backup

import android.content.Context
import android.net.Uri
import timber.log.Timber
import java.io.File
import java.util.Date

data class metadataBackup(var size: Long, var datetime: Date, var str: String)

class Backup(val context: Context) {

    init{
        Timber.i("Backup() init{}")
    }

    //val json = Json(context)

    //Получить путь до Файла Бекап
    fun getPathToBackup(): String = context.externalCacheDir.toString() + "/backup.zip"

    //Получить Uri файла бекап
    fun getURIBackup(): Uri = Uri.fromFile(File(getPathToBackup()))

    fun getMetadataBackup(): metadataBackup {
        val r = metadataBackup(-1, Date(0), "")
        val f = File(getPathToBackup())
        if (f.exists()) {
            r.size = f.length() //Размер файла
            r.datetime = Date(f.lastModified()) //Время последней модификации
            r.str =
                (1900 + r.datetime.year - 2000).toString() + "/" + r.datetime.month.toString() + "/" + r.datetime.date.toString() + " " + r.datetime.hours.toString() + ":" + r.datetime.minutes.toString() + ":" + r.datetime.seconds.toString()
        }
        return r
    }

    //https://github.com/arnab-kundu/Storage
   // private val appFileManager = AppFileManager(BuildConfig.APPLICATION_ID)

    //Сохранить /files->/cache/backup.zip
    fun createBackupZipFileToCache() { //Всю папку с данными
        val srcFolderPath = context.getExternalFilesDir("").toString()
        context.externalCacheDir?.let { deleteDir(it) } //Сохранить в папку кеш
        val destFolderPath = context.externalCacheDir.toString()
  //      appFileManager.zipFiles(srcFolderPath, "$destFolderPath/backup.zip")
    }

    //Разорхивировать /cache/backup.zip->/files
    fun unZipFileFromCache() {
        val extractLocationPath =
            context.getExternalFilesDir("").toString().substringBeforeLast('/')
 //       appFileManager.unZipFile(
 //           zipFilePath = getPathToBackup(), extractLocationPath = extractLocationPath
 //       )
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            for (child in dir.listFiles()!!) {
                val success = deleteDir(child)
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }

/////////////////////////

}
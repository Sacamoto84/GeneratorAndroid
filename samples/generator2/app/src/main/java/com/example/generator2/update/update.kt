package com.example.generator2.update

import android.content.Context
import com.example.generator2.AppPath
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


object Update{


    private const val owner = "Sacamoto84"
    private const val repo  = "GeneratorAndroid"

    var isDownloading = MutableStateFlow(false)
    var isDownloaded  = MutableStateFlow(false)

    var visibleDialogNew      = MutableStateFlow(false) //Показать диалог новый

    var externalVersion = "" //Версия программы на сервере
    var currentVersion = ""  //Текущая версия программы

    var url = ""
    private var fil = AppPath().download + "/update.apk"

//    private static final String RELEASE_API_URL = "https://api.github.com/repos/Dar9586/NClientV2/releases";
//    private static final String LATEST_RELEASE_URL = "https://github.com/Dar9586/NClientV2/releases/latest";

    @OptIn(DelicateCoroutinesApi::class)
    fun run(context : Context){

        GlobalScope.launch(Dispatchers.IO) {

            currentVersion = getVersionName(context)

            //Запрос на получение текущей версии от гитара
            val tags: List<String>

            try {
                tags = gitHubReleaseTags(owner, repo)

                if (tags.isEmpty())
                    throw Exception("Отсутствуют теги")
            }
            catch (e : Exception)
            {
                Timber.e( "Update.run " + e.localizedMessage)
                return@launch
            }

            externalVersion = tags.first()

            try {
                val files = gitHubReleaseFiles(owner, repo, externalVersion)
                if (files.isEmpty())
                    throw Exception("Отсутствуют файлы в текущем $externalVersion теге")

                url = "https://github.com/${owner}/${repo}/releases/download/${externalVersion}/${files.first()}"
                Timber.i(url)

            }
            catch (e : Exception)
            {
                Timber.e( "Update.run " + e.localizedMessage)
                return@launch
            }

            //https://github.com/Dar9586/NClientV2/releases/download/3.0.1/NClientV2.3.0.1.Release.apk

            try {
                val c = currentVersion.split(".")
                val cc = c[0].toInt() * 1000 + c[1].toInt()*100 + c[2].toInt()*10 + c[3].toInt()

                val e = externalVersion.split(".")
                val ee = e[0].toInt() * 1000 + e[1].toInt()*100 + e[2].toInt()*10 + e[3].toInt()

                Timber.i("c=$c cc=$cc")
                Timber.i("e=$e ee=$ee")

                if (ee > cc)
                    visibleDialogNew.value = true
            }
           catch (e : Exception)
           {
               Timber.e(e.localizedMessage)
           }



        }

        GlobalScope.launch(Dispatchers.IO) {
            isDownloading.collect {
                if (it)
                {
                    //Требуется закачка
                    GlobalScope.launch(Dispatchers.IO)
                    {
                        downloadFile(url, File(fil))
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            isDownloaded.collect {
                if (it)
                {
                    GlobalScope.launch(Dispatchers.Main) {
                        installAPK(context, File(fil))
                    }
                }
            }
        }

    }

    /////////////////////////////////////////////////////////
    var percent = MutableStateFlow(0F)
    var downloadSize :Long = 0L
    var downloadedByte :Long = 0L

    private fun downloadFile(url: String, file: File) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        response.body.let { responseBody ->
            downloadSize = responseBody.contentLength()
            FileOutputStream(file).use { outputStream ->
                responseBody.byteStream().use { inputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead = inputStream.read(buffer)
                    while (bytesRead != -1) {
                        downloadedByte += bytesRead
                        percent.value = downloadedByte.toFloat()/ downloadSize.toFloat()
                        outputStream.write(buffer, 0, bytesRead)
                        bytesRead = inputStream.read(buffer)

                        if (!Update.isDownloading.value)
                            return@let

                    }
                    outputStream.flush()
                    isDownloaded.value = true
                }
            }
        }
    }




}






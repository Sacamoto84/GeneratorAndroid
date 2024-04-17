package com.example.generator2.features.update

import android.content.Context
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.features.update.mono.getVersionName
import com.example.generator2.features.update.mono.gitHubReleaseFiles
import com.example.generator2.features.update.mono.gitHubReleaseTags
import com.example.generator2.features.update.mono.installAPK
import com.example.generator2.noSQL.KEY_NOSQL_CONFIG2
import com.kdownloader.KDownloader
import com.yandex.metrica.YandexMetrica
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import lan.ping
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

lateinit var kDownloader: KDownloader

enum class UPDATESTATE {
    NONE,
    DOWNLOADING, //Запуск скачивания
    DOWNLOADED,
}

@Singleton
class Update @Inject constructor(
    val appPath: AppPath,
    val global: Global,
    @ApplicationContext val context: Context
) {

    private val owner = "Sacamoto84"
    private val repo = "GeneratorAndroid"

    var state = MutableStateFlow(UPDATESTATE.NONE)

    /**
     * Версия программы на сервере в виде строки "2.0.0.7"
     */
    var externalVersion = "" //Версия программы на сервере

    /**
     * Текущая версия программы в виде строки "2.0.0.7"
     */
    var currentVersion = ""  //Текущая версия программы


    private var url = ""
    private var fil = appPath.download + "/update.apk"

//    private static final String RELEASE_API_URL = "https://api.github.com/repos/Dar9586/NClientV2/releases";
//    private static final String LATEST_RELEASE_URL = "https://github.com/Dar9586/NClientV2/releases/latest";

    /**
     * Автообновление
     */
    var autoupdate = MutableStateFlow( false)




    //Сохранить автосохранение
    fun autoupdate(onoff : Boolean = false){
        autoupdate.value = onoff
        global.noSQLConfig2.write(KEY_NOSQL_CONFIG2.UPDATEAUTO.value, onoff)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun run() {

        GlobalScope.launch(Dispatchers.IO) {

            autoupdate.value = global.noSQLConfig2.read(KEY_NOSQL_CONFIG2.UPDATEAUTO.value, false)

            currentVersion = getVersionName(context) //"2.0.0.7"

            //Запрос на получение текущей версии от гита
            val tags: List<String>

            try {
                tags = gitHubReleaseTags(owner, repo)

                if (tags.isEmpty())
                    throw Exception("Отсутствуют теги")
            } catch (e: Exception) {
                YandexMetrica.reportError("Update run Отсутствуют теги", e)
                Timber.e("Update.run " + e.localizedMessage)
                return@launch
            }

            externalVersion = tags.first() //"2.0.0.7"

            try {
                val files =
                    gitHubReleaseFiles(owner, repo, externalVersion)  //"2.0.0.6.Release.apk"
                if (files.isEmpty())
                    throw Exception("Отсутствуют файлы в текущем $externalVersion теге")

                //https://github.com/Sacamoto84/GeneratorAndroid/releases/download/2.0.0.6/2.0.0.6.Release.apk
                url =
                    "https://github.com/$owner/$repo/releases/download/$externalVersion/${files.first()}"
                Timber.i(url)

            } catch (e: Exception) {
                YandexMetrica.reportError(
                    "Update run Отсутствуют файлы в текущем $externalVersion теге",
                    e
                )
                Timber.e("Update.run " + e.localizedMessage)
                return@launch
            }

            val s3url = "http://77.91.87.34:10000/gen3/$externalVersion.Release.apk"

            if (ping(s3url))
                url = s3url
            else
                YandexMetrica.reportEvent(
                    "Update", "Отсутствуют файл в S3 $externalVersion.Release.apk"
                )

            //url = "http://77.91.87.34:10000/gen3/2.4.0.0.Release.apk"

            //Определение веса версий
            try {
                val c = currentVersion.split(".")
                val cc = c[0].toInt() * 1000 + c[1].toInt() * 100 + c[2].toInt() * 10 + c[3].toInt()

                val e = externalVersion.split(".")
                val ee = e[0].toInt() * 1000 + e[1].toInt() * 100 + e[2].toInt() * 10 + e[3].toInt()

                Timber.i("c=$c cc=$cc")
                Timber.i("e=$e ee=$ee")

                //cc=2007 ee=2006
                if ((ee > cc) and (autoupdate.value))
                    state.value = UPDATESTATE.DOWNLOADING

                //visibleDialogNew.value = true //Показ диалога обновления

            } catch (e: Exception) {
                YandexMetrica.reportError("Update run ошибка определения номера версии", e)
                Timber.e(e.localizedMessage)
            }

        }



        GlobalScope.launch(Dispatchers.IO) {

            state.collect {
                when (it) {

                    UPDATESTATE.DOWNLOADING -> {
                        //downloadFile(url, File(fil))

                        if (url == "")
                            return@collect

                        val request = kDownloader
                            .newRequestBuilder(url, appPath.download, "update.apk")
                            .tag("TAG")
                            .build()

                        kDownloader.enqueue(
                            request,
                            onStart = {
                                println("Запуск закачки")
                            },
                            onProgress = { it1 ->
                                println("progress $it1")
                                percent.value = it1 / 100f
                            },
                            onCompleted = {
                                println("onCompleted закачки")
                                state.value = UPDATESTATE.DOWNLOADED //Загрузка завершена
                            },
                        )

                    }

                    UPDATESTATE.DOWNLOADED -> {
                        installAPK(context, File(fil))
                    }

                    else -> {}
                }
            }

        }


    }

    /////////////////////////////////////////////////////////
    var percent = MutableStateFlow(0F) //Процент скачанного файла от 0..1
    var downloadSize: Long = 0L
    var downloadedByte: Long = 0L

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
                        percent.value = downloadedByte.toFloat() / downloadSize.toFloat()
                        outputStream.write(buffer, 0, bytesRead)
                        bytesRead = inputStream.read(buffer)
                    }
                    outputStream.flush()
                    state.value = UPDATESTATE.DOWNLOADED //Загрузка завершена
                }
            }
        }
    }


}






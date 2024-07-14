package com.example.generator2.features.initialization

import android.content.Context
import androidx.media3.common.util.UnstableApi
import cafe.adriel.pufferdb.android.AndroidPufferDB
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.PermissionStorage
import com.example.generator2.application
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.explorer.domen.explorerInitialization
import com.example.generator2.features.initialization.utils.listFilesInAssetsFolder
import com.example.generator2.features.presets.presetsInit
import com.example.generator2.features.presets.presetsReadFile
import com.example.generator2.features.presets.presetsToLiveData
import com.example.generator2.features.update.kDownloader
import com.example.generator2.model.itemList
import com.example.generator2.features.generator.observe
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.toast
import com.kdownloader.KDownloader
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.measureTimeMillis

private const val TAG = "initialization"

@androidx.annotation.OptIn(UnstableApi::class)
class Initialization
    (
    val context: Context,
    val utils: UtilsKT,
    val appPath: AppPath,
    val global: Global,

    val audioMixerPump: AudioMixerPump,

) {

    var isInitialized = false  //Признак того что произошла инициализация

    //lateinit var s0: Deferred<Unit>

    lateinit var s1: Deferred<Unit>
    lateinit var s2: Deferred<Unit>

    lateinit var s3: Deferred<Unit>
    lateinit var s4: Deferred<Unit>
    lateinit var s5: Deferred<Unit>

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun run() {


        Timber.tag("Время работы").i("!!! Инициализация начало !!!")
        val startTime = System.currentTimeMillis()

//        /* S0 */
//        s0 = GlobalScope.async(Dispatchers.IO) {
//            Timber.tag("Время работы").i("S0 start")
//            val t = measureTimeMillis {
//                try {
//                    //AssetCopier(context).copy("Carrier", File(patchCarrier))
//                    AssetCopier(context).copy("Mod", File(appPath.mod))
//                } catch (e: IOException) {
//                    Timber.e(e.printStackTrace().toString())
//                }
//            }
//            Timber.tag("Время работы").i("S0 Stop Время инициализации AssetCopier : $t ms") //180ms
//        }

        /* explorer */
        GlobalScope.launch(Dispatchers.IO) {
            val t = measureTimeMillis {
                explorerInitialization(context)
            }
            Timber.tag("Время работы").i("Время инициализации explorer: $t ms") //6350ms на 157 файлов
        }

        /* S3 */
        s3 = GlobalScope.async(Dispatchers.IO) {
            val t = measureTimeMillis {
                Timber.tag("Время работы").i("S3 start")
                //kDownloader = KDownloader.create(application)
            }
            Timber.tag("Время работы")
                .i("S3 stop Время инициализации : $t ms [kDownloader]") //21ms  //79ms на S7
        }

        /* S4 */
        s4 = GlobalScope.async(Dispatchers.IO) {
            val t = measureTimeMillis {
                Timber.tag("Время работы").i("S4 start")
                AndroidPufferDB.init(application)
                presetsInit(appPath)
            }
            Timber.tag("Время работы")
                .i("S4 stop Время инициализации : $t ms [AndroidPufferDB, presetsInit]") //21ms  //108ms на S7
        }


        val path = appPath
        //path.mkDir()

        val patchCarrier = appPath.assets + "/Carrier"   //path.carrier
        val patchMod = appPath.assets + "/Mod"

        Utils.patchDocument = path.main
        Utils.patchCarrier = patchCarrier
        Utils.patchMod = "$patchMod/"

        //s0.await()

//        s1 = GlobalScope.async(Dispatchers.Main) {
//            val t = measureTimeMillis {
//                Timber.tag("Время работы").i("firstDeferred start")
//                val arrFilesCarrier = listFilesInAssetsFolder(application, "Carrier")
//                for (i in arrFilesCarrier.indices) {
//                    audioMixerPump.gen.itemlistCarrier.add(itemList("Carrier", arrFilesCarrier[i], 0))
//                }
//            }
//            Timber.tag("Время работы").i("firstDeferred stop : $t ms")
//        }
//
//        s2 = GlobalScope.async(Dispatchers.IO) {
//            val t111 = measureTimeMillis {
//                Timber.tag("Время работы").i("secondDeferred start")
//                val arrFilesMod = listFilesInAssetsFolder(application, "Mod")
//                    //listFileInDir(appPath.mod) //Получение списка файлов в папке Mod //6ms
//                for (i in arrFilesMod.indices) {
//                    audioMixerPump.gen.itemlistAM.add(itemList("Mod", arrFilesMod[i], 1)) //648ms -> 369 -> 207
//                    audioMixerPump.gen.itemlistFM.add(itemList("Mod", arrFilesMod[i], 0)) // all 65ms
//                }
//            }
//            Timber.tag("Время работы").i("secondDeferred stop : $t111 ms")
//        }


        val t4 = measureTimeMillis {

            Timber.tag("Время работы").i("t4 start")
            //Инициализация
            if ((!isInitialized) && (PermissionStorage.hasPermissions(context))) {

                Timber.tag("Время работы").i("t4 1")
                toast.initialized(context) //0 ms
                Timber.tag("Время работы").i("t4 2")

                Timber.tag("Время работы").i("t4 3")
                global.mmkv.readConstrain() //4ms
                Timber.tag("Время работы").i("t4 4")
                presetsToLiveData(presetsReadFile("default", path = appPath.config), audioMixerPump.gen) //67ms
                Timber.tag("Время работы").i("t4 5")

                //Проверка поддержки 192k
                audioMixerPump.audioOut.checkSupport192k() //4ms

                Timber.tag("Время работы").i("t4 6")
            }


        }
        Timber.tag("Время работы").i("4 stop Время инициализации : $t4 ms") //45ms


        audioMixerPump.initializationGen()

        //s1.await()
        //s2.await()
        observe(audioMixerPump.gen) //30ms
        s3.await()
        s4.await()


        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        println("Время выполнения кода: $elapsedTime мс")
        Timber.tag("Время работы").i("!!! Инициализация завершена: $elapsedTime мс!!!")

        isInitialized = true

    }
}



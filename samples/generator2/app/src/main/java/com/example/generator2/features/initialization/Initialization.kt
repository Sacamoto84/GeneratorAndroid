package com.example.generator2.features.initialization

import android.content.Context
import cafe.adriel.pufferdb.android.AndroidPufferDB
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.PermissionStorage
import com.example.generator2.audio.checkSupport192k
import com.example.generator2.features.explorer.domen.explorerInitialization
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.presets.presetsInit
import com.example.generator2.features.presets.presetsReadFile
import com.example.generator2.features.presets.presetsToLiveData
import com.example.generator2.features.update.kDownloader
import com.example.generator2.model.itemList
import com.example.generator2.observe
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.toast
import com.kdownloader.KDownloader
import flipagram.assetcopylib.AssetCopier
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

private const val TAG = "initialization"

class Initialization(
    val context: Context,
    val gen: Generator,
    val utils: UtilsKT,
    val appPath: AppPath,
    val global: Global
) {

    var isInitialized = false  //Признак того что произошла инициализация

    lateinit var firstDeferred: Deferred<Unit>
    lateinit var secondDeferred: Deferred<Unit>


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun run() {

        Timber.plant(Timber.DebugTree())
        Timber.tag("Время работы").i("!!! Инициализация начало !!!")
        val startTime = System.currentTimeMillis()

        GlobalScope.launch(Dispatchers.IO) {
            val executionTime1 = measureTimeMillis {
                explorerInitialization(context)
            }
            Timber.tag("Время работы").i("Время инициализации explorer: $executionTime1 ms") //6350ms на 157 файлов
        }

        println("Типа инициализация Splash")
        val path = appPath
        //path.mkDir()

        val patchCarrier = appPath.assets+"/Carrier/"//path.carrier
        val patchMod = path.mod

        Utils.patchDocument = path.main
        Utils.patchCarrier = patchCarrier
        Utils.patchMod = "$patchMod/"

        val t1 = measureTimeMillis {
            try {
                //AssetCopier(context).copy("Carrier", File(patchCarrier))
                AssetCopier(context).copy("Mod", File(patchMod))
            } catch (e: IOException) {
                Timber.e(e.printStackTrace().toString())
            }
        }
        Timber.tag("Время работы").i("1 Время инициализации AssetCopier : $t1 ms") //180ms


        val executionTime = measureTimeMillis {

            firstDeferred = GlobalScope.async(Dispatchers.IO) {

                println("arrFilesCarrier start")
                val arrFilesCarrier: List<String> = listFilesInAssetsFolder(context, "Carrier")
                ////////////////////////////////////////////////////////////////// //Utils.listFileInCarrier() //Заполняем список
                for (i in arrFilesCarrier.indices) {
                    gen.itemlistCarrier.add(itemList(patchCarrier, arrFilesCarrier[i], 0))
                }
                ////67ms 74ms

            }

            secondDeferred = GlobalScope.async(Dispatchers.IO){

                val arrFilesMod: Array<String> =
                    Utils.listFileInMod() //Получение списка файлов в папке Mod //6ms

                for (i in arrFilesMod.indices) {
                    gen.itemlistAM.add(itemList(patchMod, arrFilesMod[i], 1)) //648ms -> 369 -> 207
                    gen.itemlistFM.add(itemList(patchMod, arrFilesMod[i], 0)) // all 65ms
                }

            }


        }
        Timber.tag("Время работы").i("2 Время инициализации itemList bitmap: $executionTime ms") //660ms -> 365ms


        val t3 = measureTimeMillis {
            kDownloader = KDownloader.create(context)
            AndroidPufferDB.init(context)
            presetsInit(appPath)
        }
        Timber.tag("Время работы").i("3 Время инициализации : $t3 ms") //21ms



        val t4 = measureTimeMillis {
//Инициализация
            if ((!isInitialized) && (PermissionStorage.hasPermissions(context))) {

                Timber.i(TAG, "Типа инициализация Start")

                toast.initialized(context)

                //GlobalScope.launch(Dispatchers.IO) {


                // val path = AppPath()
                // path.mkDir()

//            val patchCarrier = path.carrier
//            val patchMod = path.mod
//
//            Utils.patchDocument = path.main
//            Utils.patchCarrier = "$patchCarrier/"
//            Utils.patchMod = "$patchMod/"

//            try {
//                AssetCopier(context).copy("Carrier", File(patchCarrier))
//                AssetCopier(context).copy("Mod", File(patchMod))
//            } catch (e: IOException) {
//                Timber.e(e.printStackTrace().toString())
//            }

//            Timber.i("arrFilesCarrier start")
//            val arrFilesCarrier: Array<String> = Utils.listFileInCarrier() //Заполняем список
//            for (i in arrFilesCarrier.indices) {
//                gen.itemlistCarrier.add(itemList(patchCarrier, arrFilesCarrier[i], 0))
//            }

//            val arrFilesMod: Array<String> =
//                Utils.listFileInMod() //Получение списка файлов в папке Mod
//            for (i in arrFilesMod.indices) {
//                gen.itemlistAM.add(itemList(patchMod, arrFilesMod[i], 1))
//                gen.itemlistFM.add(itemList(patchMod, arrFilesMod[i], 0))
//            }
//            Timber.i("arrFilesCarrier end")

                observe(utils, gen)

                global.mmkv.readConstrain()

                presetsToLiveData(presetsReadFile("default", path = appPath.config), gen)

                //mmkv.readVolume()

                //Проверка поддержки 192k
                checkSupport192k()

                isInitialized = true

                Timber.i("initialization", "Типа инициализация End")
                //}

            }






        }
        Timber.tag("Время работы").i("4 Время инициализации : $t4 ms") //45ms


        firstDeferred.await()
        secondDeferred.await()

        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        println("Время выполнения кода: $elapsedTime мс")
        Timber.tag("Время работы").i("!!! Инициализация завершена: $elapsedTime мс!!!")

    }
}

/**
 * Получение списка файлов в папке Assets
 */
fun listFilesInAssetsFolder(context: Context, folderName: String = "Carrier"): List<String> {
    val assetManager = context.assets
    try {
        return assetManager.list(folderName)?.toList() ?: emptyList()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return emptyList()
}

package com.example.generator2

import android.content.Context
import cafe.adriel.pufferdb.android.AndroidPufferDB
import com.example.generator2.audio.checkSupport192k
import com.example.generator2.features.explorer.domen.explorerFilterMediaType
import com.example.generator2.features.explorer.domen.explorerGetAllMusicFiles
import com.example.generator2.features.explorer.domen.explorerInitialization
import com.example.generator2.features.explorer.domen.explorerTreeBuild
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.presets.presetsInit
import com.example.generator2.features.presets.presetsReadFile
import com.example.generator2.features.presets.presetsToLiveData
import com.example.generator2.features.update.kDownloader
import com.example.generator2.model.itemList
import com.example.generator2.model.traverseTree
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.toast
import com.kdownloader.KDownloader
import flipagram.assetcopylib.AssetCopier
import timber.log.Timber
import java.io.File
import java.io.IOException

private const val TAG = "initialization"

class Initialization(
    val context: Context,
    val gen: Generator,
    val utils: UtilsKT,
    val appPath: AppPath,
    val global: Global
) {

    var isInitialized = false  //Признак того что произошла инициализация

    fun run() {

        Timber.plant(Timber.DebugTree())

        explorerInitialization(context)


//        GlobalScope.launch(Dispatchers.IO) {
//            println("Запуск Yandex Metrika")
//            val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build()
//            YandexMetrica.activate(context, config)
//            YandexMetrica.enableActivityAutoTracking(context as Application)
//            YandexMetrica.reportEvent("Запуск")
//        }

        println("Типа инициализация Splash")
        val path = appPath
        //path.mkDir()

        val patchCarrier = path.carrier
        val patchMod = path.mod

        Utils.patchDocument = path.main
        Utils.patchCarrier = "$patchCarrier/"
        Utils.patchMod = "$patchMod/"

        try {
            AssetCopier(context).copy("Carrier", File(patchCarrier))
            AssetCopier(context).copy("Mod", File(patchMod))
        } catch (e: IOException) {
            Timber.e(e.printStackTrace().toString())
        }

        println("arrFilesCarrier start")
        val arrFilesCarrier: Array<String> = Utils.listFileInCarrier() //Заполняем список
        for (i in arrFilesCarrier.indices) {
            gen.itemlistCarrier.add(itemList(patchCarrier, arrFilesCarrier[i], 0))
        }

        val arrFilesMod: Array<String> =
            Utils.listFileInMod() //Получение списка файлов в папке Mod
        for (i in arrFilesMod.indices) {
            gen.itemlistAM.add(itemList(patchMod, arrFilesMod[i], 1))
            gen.itemlistFM.add(itemList(patchMod, arrFilesMod[i], 0))
        }

        kDownloader = KDownloader.create(context)

        AndroidPufferDB.init(context)
        presetsInit(appPath)


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
}


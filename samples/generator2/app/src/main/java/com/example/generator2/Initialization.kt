package com.example.generator2

import android.content.Context
import com.example.generator2.generator.Generator
import com.example.generator2.model.itemList
import com.example.generator2.presets.presetsReadFile
import com.example.generator2.presets.presetsToLiveData
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.toast
import flipagram.assetcopylib.AssetCopier
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException

var isInitialized = false  //Признак того что произошла инициализация

@OptIn(DelicateCoroutinesApi::class)
fun initialization(context: Context, gen: Generator, utils: UtilsKT) {

//Инициализация
    if ((!isInitialized) && (PermissionStorage.hasPermissions(context))) {

        toast.initialized(context)

        GlobalScope.launch(Dispatchers.IO) {
            Timber.i("Типа инициализация Start")

            val path = AppPath()
            path.mkDir()

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

            Timber.i("arrFilesCarrier start")
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
            Timber.i("arrFilesCarrier end")

            observe(utils, gen)

            mmkv.readConstrain()

            presetsToLiveData(presetsReadFile("default", path = AppPath().config), gen)

            //mmkv.readVolume()

            isInitialized = true

            Timber.i("Типа инициализация End")
        }

    }


}


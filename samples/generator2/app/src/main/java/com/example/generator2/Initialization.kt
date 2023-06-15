package com.example.generator2

import android.content.Context
import com.example.generator2.di.Hub
import com.example.generator2.model.LiveData
import com.example.generator2.model.itemList
import com.example.generator2.model.itemlistAM
import com.example.generator2.model.itemlistCarrier
import com.example.generator2.model.itemlistFM
import com.example.generator2.model.mmkv
import com.example.generator2.util.Utils
import flipagram.assetcopylib.AssetCopier
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException

var isInitialized = false  //Признак того что произошла инициализация
var isInitialized1 = false //Признак того что произошла инициализация
var isInitialized2 = false //Признак того что произошла инициализация

@OptIn(DelicateCoroutinesApi::class)
fun initialization(context: Context, hub: Hub) {

//Инициализация
    if ((!isInitialized) && (PermissionStorage.hasPermissions(context))) {

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
                itemlistCarrier.add(itemList(patchCarrier, arrFilesCarrier[i], 0))
            }
            val arrFilesMod: Array<String> =
                Utils.listFileInMod() //Получение списка файлов в папке Mod
            for (i in arrFilesMod.indices) {
                itemlistAM.add(itemList(patchMod, arrFilesMod[i], 1))
                itemlistFM.add(itemList(patchMod, arrFilesMod[i], 0))
            }
            Timber.i("arrFilesCarrier end")

            isInitialized1 = true

            Timber.i("Типа инициализация End")
        }

        GlobalScope.launch(Dispatchers.IO) {

            val numDeferred1 = async {
                //hub.backup.json.readJsonConfig()
                //Puffer().readConfig()
                mmkv.readConfig()
                true
            }
            val numDeferred2 = async {
                //hub.backup.json.readJsonConstrain()
                mmkv.readConstrain()
                true
            }
            val numDeferred3 = async {
                //hub.backup.json.readJsonVolume()
                mmkv.readVolume()
                true
            }

            val numDeferred4 = async {
                //hub.backup.json.readJsonVolume()
                mmkv.readImpulse()
                true
            }


            isInitialized2 = numDeferred1.await() and numDeferred2.await() and numDeferred3.await() and numDeferred4.await()

        }

        GlobalScope.launch(Dispatchers.IO) {
            while (!isInitialized) {
                delay(10)
                isInitialized = isInitialized1 and isInitialized2
            }

            hub.audioDevice.sendAlltoGen()
            observe(hub)
            hub.playbackEngine.StartListening()

        }



    }


}


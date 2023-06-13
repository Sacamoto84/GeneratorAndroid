package com.example.generator2.backup

import android.content.Context
import com.example.generator2.model.LiveConstrain
import com.example.generator2.model.LiveData
import com.google.gson.Gson
import java.io.File

private data class DataJsonVolume(
    var maxVolume0 : Float = 0.9f,
    var maxVolume1 : Float = 0.9f,
)

private data class DataJsonConstrain(
    var sensetingSliderCr: Float = 0f,
    var sensetingSliderFmDev: Float = 0f,
    var sensetingSliderFmBase: Float = 0f,
    var sensetingSliderAmFm: Float = 0f,
    var minCR: Float = 0f,
    var maxCR: Float = 0f,
    var minModAmFm: Float = 0f,
    var maxModAmFm: Float = 0f,
    var minFMBase: Float = 0f,
    var maxFMBase: Float = 0f,
    var minFMDev: Float = 0f,
    var maxFMDev: Float = 0f,
)

private data class DataJsonConfig(
    var ch1_EN: Boolean = false,
    var ch1_Carrier_Filename: String = "Sine",
    var ch1_Carrier_Fr: Float = 400.0f,
    var ch1_AM_EN: Boolean = false,
    var ch1_AM_Filename: String = "09_Ramp",
    var ch1_AM_Fr: Float = 8.7f,
    var ch1_FM_EN: Boolean = false,
    var ch1_FM_Filename: String = "06_CHIRP",
    var ch1_FM_Base: Float = 2500f,
    var ch1_FM_Dev: Float = 1100f,
    var ch1_FM_Fr: Float = 5.1f,

    var ch2_EN: Boolean = false,
    var ch2_Carrier_Filename: String = "Sine",
    var ch2_Carrier_Fr: Float = 2000.0f,
    var ch2_AM_EN: Boolean = false,
    var ch2_AM_Filename: String = "09_Ramp",
    var ch2_AM_Fr: Float = 8.7f,
    var ch2_FM_EN: Boolean = false,
    var ch2_FM_Filename: String = "06_CHIRP",
    var ch2_FM_Base: Float = 2500f,
    var ch2_FM_Dev: Float = 1100f,
    var ch2_FM_Fr: Float = 5.1f,

    var mono: Boolean = false,
    var invert: Boolean = false,

    var shuffle: Boolean = false,

    var enL: Boolean = true,
    var enR: Boolean = true,

    var currentVolume0: Float = 1.0f,
    var currentVolume1: Float = 1.0f,

    )


class Json(val context: Context) {
    //Адрес файла текущей конфигурации

    private val pathCurrentConfig = context.getExternalFilesDir("/Config").toString() + "/CurrentConfig1.json"
    private val iniCurrentVolume = context.getExternalFilesDir("/Config").toString() + "/Volume1.json"
    private val iniCurrentConstrain = context.getExternalFilesDir("/Config").toString() + "/Constrain1.json"


    /////////////////////////
    fun readJsonVolume() {
        print("readJsonVolume..")
        if (!File(iniCurrentVolume).exists()) {
            saveJsonVolume()
            //return
        }
        val s = File(iniCurrentVolume).readText()
        val dataJsonVolume = Gson().fromJson(s, DataJsonVolume::class.java)

        LiveData.maxVolume0.value = dataJsonVolume.maxVolume0
        LiveData.maxVolume1.value = dataJsonVolume.maxVolume1

        LiveData.volume0.value = LiveData.currentVolume0.value * dataJsonVolume.maxVolume0
        LiveData.volume1.value = LiveData.currentVolume1.value * dataJsonVolume.maxVolume1

        println("ok")
    }

    fun saveJsonVolume() {
        print("saveJsonVolume..")
        val dataJsonVolume = DataJsonVolume(LiveData.maxVolume0.value, LiveData.maxVolume1.value)
        val jsonString = Gson().toJson(dataJsonVolume, DataJsonVolume::class.java )  // json string
        File(iniCurrentVolume).writeText(jsonString)
        println("ok")
    }
    /////////////////////////

    fun saveJsonConstrain() {

        print("saveJsonConstrain..")
        val dataJsonConstrain = DataJsonConstrain(
            sensetingSliderCr = LiveConstrain.sensetingSliderCr.value,
            sensetingSliderFmDev = LiveConstrain.sensetingSliderFmDev.value,
            sensetingSliderFmBase = LiveConstrain.sensetingSliderFmBase.value,
            sensetingSliderAmFm = LiveConstrain.sensetingSliderAmFm.value,
            minCR = LiveConstrain.minCR.value,
            maxCR = LiveConstrain.maxCR.value,
            minModAmFm = LiveConstrain.minModAmFm.value,
            maxModAmFm = LiveConstrain.maxModAmFm.value,
            minFMBase = LiveConstrain.minFMBase.value,
            maxFMBase = LiveConstrain.maxFMBase.value,
            minFMDev = LiveConstrain.minFMDev.value,
            maxFMDev = LiveConstrain.maxFMDev.value,
        )

        val jsonString = Gson().toJson(dataJsonConstrain)  // json string
        File(iniCurrentConstrain).writeText(jsonString)
        println("ok")
    }

    fun readJsonConstrain() {
        print("readJsonConstrain..")
        if (!File(iniCurrentConstrain).exists()) {
            saveJsonConstrain()
            //return
        }

        val s = File(iniCurrentConstrain).readText()
        val dataJsonVolume = Gson().fromJson(s, DataJsonConstrain::class.java)

        LiveConstrain.sensetingSliderCr.value = dataJsonVolume.sensetingSliderCr
        LiveConstrain.sensetingSliderFmDev.value = dataJsonVolume.sensetingSliderFmDev
        LiveConstrain.sensetingSliderFmBase.value = dataJsonVolume.sensetingSliderFmBase
        LiveConstrain.sensetingSliderAmFm.value = dataJsonVolume.sensetingSliderAmFm
        LiveConstrain.minCR.value = dataJsonVolume.minCR
        LiveConstrain.maxCR.value = dataJsonVolume.maxCR
        LiveConstrain.minModAmFm.value = dataJsonVolume.minModAmFm
        LiveConstrain.maxModAmFm.value = dataJsonVolume.maxModAmFm
        LiveConstrain.minFMBase.value = dataJsonVolume.minFMBase
        LiveConstrain.maxFMBase.value = dataJsonVolume.maxFMBase
        LiveConstrain.minFMBase.value = dataJsonVolume.minFMDev
        LiveConstrain.maxFMDev.value = dataJsonVolume.maxFMDev

        println("ok")
    }
    /////////////////////////

    fun saveJsonConfig() {

        print("saveJsonConfig..")

        val dataJsonConstrain = DataJsonConfig(

            ch1_EN = LiveData.ch1_EN.value,
            ch1_Carrier_Filename = LiveData.ch1_Carrier_Filename.value,
            ch1_Carrier_Fr = LiveData.ch1_Carrier_Fr.value,
            ch1_AM_EN = LiveData.ch1_AM_EN.value,
            ch1_AM_Filename = LiveData.ch1_AM_Filename.value,
            ch1_AM_Fr = LiveData.ch1_AM_Fr.value,
            ch1_FM_EN = LiveData.ch1_FM_EN.value,
            ch1_FM_Filename = LiveData.ch1_FM_Filename.value,
            //ch1_FM_Base = LiveData.ch1_FM_Base.value,
            ch1_FM_Dev = LiveData.ch1_FM_Dev.value,
            ch1_FM_Fr = LiveData.ch1_FM_Fr.value,

            ch2_EN = LiveData.ch2_EN.value,
            ch2_Carrier_Filename = LiveData.ch2_Carrier_Filename.value,
            ch2_Carrier_Fr = LiveData.ch2_Carrier_Fr.value,
            ch2_AM_EN = LiveData.ch2_AM_EN.value,
            ch2_AM_Filename = LiveData.ch2_AM_Filename.value,
            ch2_AM_Fr = LiveData.ch2_AM_Fr.value,
            ch2_FM_EN = LiveData.ch2_FM_EN.value,
            ch2_FM_Filename = LiveData.ch2_FM_Filename.value,
            //ch2_FM_Base = LiveData.ch2_FM_Base.value,
            ch2_FM_Dev = LiveData.ch2_FM_Dev.value,
            ch2_FM_Fr = LiveData.ch2_FM_Fr.value,

            mono = LiveData.mono.value,
            invert = LiveData.invert.value,
            shuffle = LiveData.shuffle.value,
            enL = LiveData.enL.value,
            enR = LiveData.enR.value,

            currentVolume0 = LiveData.currentVolume0.value,
            currentVolume1 = LiveData.currentVolume1.value
        )
        val jsonString = Gson().toJson(dataJsonConstrain)  // json string
        File(pathCurrentConfig).writeText(jsonString)

        println("ok")
    }

    fun readJsonConfig() {

        print("readJsonConfig..")

        if (!File(pathCurrentConfig).exists()) {
            saveJsonConfig()
            //return
        }

        val s = File(pathCurrentConfig).readText()
        val dataJsonVolume = Gson().fromJson(s, DataJsonConfig::class.java)

        LiveData.ch1_EN.value = dataJsonVolume.ch1_EN
        LiveData.ch1_Carrier_Filename.value = dataJsonVolume.ch1_Carrier_Filename
        LiveData.ch1_Carrier_Fr.value = dataJsonVolume.ch1_Carrier_Fr
        LiveData.ch1_AM_EN.value = dataJsonVolume.ch1_AM_EN
        LiveData.ch1_AM_Filename.value = dataJsonVolume.ch1_AM_Filename
        LiveData.ch1_AM_Fr.value = dataJsonVolume.ch1_AM_Fr
        LiveData.ch1_FM_EN.value = dataJsonVolume.ch1_FM_EN
        LiveData.ch1_FM_Filename.value = dataJsonVolume.ch1_FM_Filename
        //LiveData.ch1_FM_Base.value = dataJsonVolume.ch1_FM_Base
        LiveData.ch1_FM_Dev.value = dataJsonVolume.ch1_FM_Dev
        LiveData.ch1_FM_Fr.value = dataJsonVolume.ch1_FM_Fr

        LiveData.ch2_EN.value = dataJsonVolume.ch2_EN
        LiveData.ch2_Carrier_Filename.value = dataJsonVolume.ch2_Carrier_Filename
        LiveData.ch2_Carrier_Fr.value = dataJsonVolume.ch2_Carrier_Fr
        LiveData.ch2_AM_EN.value = dataJsonVolume.ch2_AM_EN
        LiveData.ch2_AM_Filename.value = dataJsonVolume.ch2_AM_Filename
        LiveData.ch2_AM_Fr.value = dataJsonVolume.ch2_AM_Fr
        LiveData.ch2_FM_EN.value = dataJsonVolume.ch2_FM_EN
        LiveData.ch2_FM_Filename.value = dataJsonVolume.ch2_FM_Filename
        //LiveData.ch2_FM_Base.value = dataJsonVolume.ch2_FM_Base
        LiveData.ch2_FM_Dev.value = dataJsonVolume.ch2_FM_Dev
        LiveData.ch2_FM_Fr.value = dataJsonVolume.ch2_FM_Fr

        LiveData.mono.value = dataJsonVolume.mono
        LiveData.invert.value = dataJsonVolume.invert
        LiveData.shuffle.value = dataJsonVolume.shuffle

        LiveData.enL.value = dataJsonVolume.enL
        LiveData.enR.value = dataJsonVolume.enR

        LiveData.currentVolume0.value = dataJsonVolume.currentVolume0
        LiveData.currentVolume1.value = dataJsonVolume.currentVolume1

        println("ok")
    }
}













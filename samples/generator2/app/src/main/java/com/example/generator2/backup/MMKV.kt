package com.example.generator2.backup

import com.example.generator2.model.LiveConstrain
import com.example.generator2.model.LiveData
import com.tencent.mmkv.MMKV

//val rootDir = MMKV.initialize(this, AppPath().config)
//println("mmkv root: $rootDir")

class MMKv {

    val m: MMKV = MMKV.defaultMMKV()

    fun readVolume() {
        print("readPufferVolume..")

        LiveData.maxVolume0.value = m.getFloat("maxVolume0", 1.0F)
        LiveData.maxVolume1.value = m.getFloat("maxVolume1", 1.0F)

        LiveData.volume0.value = LiveData.currentVolume0.value * m.getFloat("maxVolume0", 1.0F)
        LiveData.volume1.value = LiveData.currentVolume1.value * m.getFloat("maxVolume1", 1.0F)

        println("ok")
    }

    fun saveVolume() {
        print("saveJsonVolume..")
        m.putFloat("maxVolume0", LiveData.maxVolume0.value)
        m.putFloat("maxVolume1", LiveData.maxVolume1.value)
        println("ok")
    }

    fun saveConstrain() {

        print("saveJsonConstrain..")

        m.putFloat("sensetingSliderCr", LiveConstrain.sensetingSliderCr.floatValue)
        m.putFloat("sensetingSliderFmDev", LiveConstrain.sensetingSliderFmDev.floatValue)
        m.putFloat("sensetingSliderAmFm", LiveConstrain.sensetingSliderAmFm.floatValue)

        println("ok")
    }

    fun readConstrain() {
        print("readConstrain..")

        LiveConstrain.sensetingSliderCr.floatValue = m.getFloat("sensetingSliderCr", 0.2F)
        LiveConstrain.sensetingSliderFmDev.floatValue = m.getFloat("sensetingSliderFmDev", 0.2f)
        LiveConstrain.sensetingSliderAmFm.floatValue = m.getFloat("sensetingSliderAmFm", 0.01f)

        println("ok")
    }

}
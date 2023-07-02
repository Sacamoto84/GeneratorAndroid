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

        m.putFloat("sensetingSliderCr", LiveConstrain.sensetingSliderCr.value)
        m.putFloat("sensetingSliderFmDev", LiveConstrain.sensetingSliderFmDev.value)
        m.putFloat("sensetingSliderFmBase", LiveConstrain.sensetingSliderFmBase.value)

        m.putFloat("sensetingSliderAmFm", LiveConstrain.sensetingSliderAmFm.value)
        m.putFloat("minCR", LiveConstrain.minCR.value)
        m.putFloat("maxCR", LiveConstrain.maxCR.value)
        m.putFloat("minModAmFm", LiveConstrain.minModAmFm.value)
        m.putFloat("maxModAmFm", LiveConstrain.maxModAmFm.value)
        m.putFloat("minFMBase", LiveConstrain.minFMBase.value)
        m.putFloat("maxFMBase", LiveConstrain.maxFMBase.value)
        m.putFloat("minFMDev", LiveConstrain.minFMDev.value)
        m.putFloat("maxFMDev", LiveConstrain.maxFMDev.value)

        println("ok")
    }

    fun readConstrain() {
        print("readConstrain..")

        LiveConstrain.sensetingSliderCr.value = m.getFloat("sensetingSliderCr", 0.2F)
        LiveConstrain.sensetingSliderFmDev.value = m.getFloat("sensetingSliderFmDev", 0.2f)
        LiveConstrain.sensetingSliderFmBase.value = m.getFloat("sensetingSliderFmBase", 0.2f)
        LiveConstrain.sensetingSliderAmFm.value = m.getFloat("sensetingSliderAmFm", 0.01f)
        LiveConstrain.minCR.value = m.getFloat("minCR", 600f)
        LiveConstrain.maxCR.value = m.getFloat("maxCR", 4000f)
        LiveConstrain.minModAmFm.value = m.getFloat("minModAmFm", 0.1f)
        LiveConstrain.maxModAmFm.value = m.getFloat("maxModAmFm", 100f)
        LiveConstrain.minFMBase.value = m.getFloat("minFMBase", 1000f)
        LiveConstrain.maxFMBase.value = m.getFloat("maxFMBase", 3000f)
        LiveConstrain.minFMDev.value = m.getFloat("minFMDev", 1f)
        LiveConstrain.maxFMDev.value = m.getFloat("maxFMDev", 2500f)

        println("ok")
    }

}
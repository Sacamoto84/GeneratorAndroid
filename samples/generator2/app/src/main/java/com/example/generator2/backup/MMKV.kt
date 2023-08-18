package com.example.generator2.backup

import com.example.generator2.generator.gen
import com.example.generator2.model.LiveConstrain
import com.tencent.mmkv.MMKV

//val rootDir = MMKV.initialize(this, AppPath().config)
//println("mmkv root: $rootDir")

class MMKv {

    val m: MMKV = MMKV.defaultMMKV()

    fun readVolume() {
        print("readPufferVolume..")

        gen.liveData.maxVolume0.value = m.getFloat("maxVolume0", 1.0F)
        gen.liveData.maxVolume1.value = m.getFloat("maxVolume1", 1.0F)

        gen.liveData.volume0.value = gen.liveData.currentVolume0.value * m.getFloat("maxVolume0", 1.0F)
        gen.liveData.volume1.value = gen.liveData.currentVolume1.value * m.getFloat("maxVolume1", 1.0F)

        println("ok")
    }

    fun saveVolume() {
        print("saveJsonVolume..")
        m.putFloat("maxVolume0", gen.liveData.maxVolume0.value)
        m.putFloat("maxVolume1", gen.liveData.maxVolume1.value)
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
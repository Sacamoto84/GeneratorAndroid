package com.example.generator2.backup

import com.example.generator2.model.LiveConstrain
import com.example.generator2.model.LiveData
import com.tencent.mmkv.MMKV
import timber.log.Timber

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

    fun readConfig() {

        print("readConfig..")

        LiveData.ch1_EN.value = m.getBoolean("ch1_EN", false)

        LiveData.ch1_Carrier_Filename.value =
            m.getString("ch1_Carrier_Filename", "Sine").toString()
        LiveData.ch1_Carrier_Fr.value = m.getFloat("ch1_Carrier_Fr", 400.0f)
        LiveData.ch1_AM_EN.value = m.getBoolean("ch1_AM_EN", false)
        LiveData.ch1_AM_Filename.value = m.getString("ch1_AM_Filename", "01_SINE_12b").toString()
        LiveData.ch1_AM_Fr.value = m.getFloat("ch1_AM_Fr", 5.1f)
        LiveData.ch1_FM_EN.value = m.getBoolean("ch1_FM_EN", false)
        LiveData.ch1_FM_Filename.value = m.getString("ch1_FM_Filename", "01_SINE_12b").toString()
        //LiveData.ch1_FM_Base.value = m.getFloat("ch1_FM_Base", 2500f)
        LiveData.ch1_FM_Dev.value = m.getFloat("ch1_FM_Dev", 1000f)
        LiveData.ch1_FM_Fr.value = m.getFloat("ch1_FM_Fr", 5.1f)

        LiveData.ch2_EN.value = m.getBoolean("ch2_EN", false)
        LiveData.ch2_Carrier_Filename.value =
            m.getString("ch2_Carrier_Filename", "Sine").toString()
        LiveData.ch2_Carrier_Fr.value = m.getFloat("ch2_Carrier_Fr", 400.0f)
        LiveData.ch2_AM_EN.value = m.getBoolean("ch2_AM_EN", false)
        LiveData.ch2_AM_Filename.value = m.getString("ch2_AM_Filename", "01_SINE_12b").toString()
        LiveData.ch2_AM_Fr.value = m.getFloat("ch2_AM_Fr", 5.1f)
        LiveData.ch2_FM_EN.value = m.getBoolean("ch2_FM_EN", false)
        LiveData.ch2_FM_Filename.value = m.getString("ch2_FM_Filename", "01_SINE_12b").toString()
        //LiveData.ch2_FM_Base.value = m.getFloat("ch2_FM_Base", 2500f)
        LiveData.ch2_FM_Dev.value = m.getFloat("ch2_FM_Dev", 1000f)
        LiveData.ch2_FM_Fr.value = m.getFloat("ch2_FM_Fr", 5.1f)

        LiveData.mono.value = m.getBoolean("mono", false)
        LiveData.invert.value = m.getBoolean("invert", false)
        LiveData.shuffle.value = m.getBoolean("shuffle", false)

        LiveData.enL.value = m.getBoolean("enL", true)
        LiveData.enR.value = m.getBoolean("enR", true)

        LiveData.currentVolume0.value = m.getFloat("currentVolume0", 0.9f)
        LiveData.currentVolume1.value = m.getFloat("currentVolume1", 0.9f)

        LiveData.ch1AmDepth.value = m.getFloat("ch1AmDepth", 1f)
        LiveData.ch2AmDepth.value = m.getFloat("ch2AmDepth", 1f)

        println("ok")
    }

    fun saveConfig() {

        print("saveConfig..")

        m.putBoolean("ch1_EN", LiveData.ch1_EN.value)
        m.putString("ch1_Carrier_Filename", LiveData.ch1_Carrier_Filename.value)
        m.putFloat("ch1_Carrier_Fr", LiveData.ch1_Carrier_Fr.value)
        m.putBoolean("ch1_AM_EN", LiveData.ch1_AM_EN.value)
        m.putString("ch1_AM_Filename", LiveData.ch1_AM_Filename.value)
        m.putFloat("ch1_AM_Fr", LiveData.ch1_AM_Fr.value)
        m.putBoolean("ch1_FM_EN", LiveData.ch1_FM_EN.value)
        m.putString("ch1_FM_Filename", LiveData.ch1_FM_Filename.value)
        //m.putFloat("ch1_FM_Base", LiveData.ch1_FM_Base.value)
        m.putFloat("ch1_FM_Dev", LiveData.ch1_FM_Dev.value)
        m.putFloat("ch1_FM_Fr", LiveData.ch1_FM_Fr.value)

        m.putBoolean("ch2_EN", LiveData.ch2_EN.value)
        m.putString("ch2_Carrier_Filename", LiveData.ch2_Carrier_Filename.value)
        m.putFloat("ch2_Carrier_Fr", LiveData.ch2_Carrier_Fr.value)
        m.putBoolean("ch2_AM_EN", LiveData.ch2_AM_EN.value)
        m.putString("ch2_AM_Filename", LiveData.ch2_AM_Filename.value)
        m.putFloat("ch2_AM_Fr", LiveData.ch2_AM_Fr.value)
        m.putBoolean("ch2_FM_EN", LiveData.ch2_FM_EN.value)
        m.putString("ch2_FM_Filename", LiveData.ch2_FM_Filename.value)
        //m.putFloat("ch2_FM_Base", LiveData.ch2_FM_Base.value)
        m.putFloat("ch2_FM_Dev", LiveData.ch2_FM_Dev.value)
        m.putFloat("ch2_FM_Fr", LiveData.ch2_FM_Fr.value)

        m.putBoolean("mono", LiveData.mono.value)
        m.putBoolean("invert", LiveData.invert.value)
        m.putBoolean("shuffle", LiveData.shuffle.value)
        m.putBoolean("enL", LiveData.enL.value)
        m.putBoolean("enR", LiveData.enR.value)
        m.putFloat("currentVolume0", LiveData.currentVolume0.value)
        m.putFloat("currentVolume1", LiveData.currentVolume1.value)

        m.putFloat("ch1AmDepth", LiveData.ch1AmDepth.value)
        m.putFloat("ch2AmDepth", LiveData.ch2AmDepth.value)

        println("ok")

    }

    fun readImpulse() {
        Timber.i("readImpulse..")

        LiveData.impulse0.value = m.getBoolean("impulse0", false)
        LiveData.impulse1.value = m.getBoolean("impulse1", false)

        // Ширина импульса в тиках
        LiveData.impulse0timeImp.value = m.getInt("impulse0timeImp", 9)
        LiveData.impulse1timeImp.value = m.getInt("impulse1timeImp", 9)

        // Пауза в тиках
        LiveData.impulse0timeImpPause.value = m.getInt("impulse0timeImpPause", 9)
        LiveData.impulse1timeImpPause.value = m.getInt("impulse1timeImpPause", 9)

        println("ok")
    }

    fun saveImpulse() {
        print("saveJsonVolume..")

        m.putBoolean("impulse0", LiveData.impulse0.value)
        m.putBoolean("impulse1", LiveData.impulse1.value)

        // Ширина импульса в тиках
        m.putInt("impulse0timeImp", LiveData.impulse0timeImp.value )
        m.putInt("impulse1timeImp", LiveData.impulse1timeImp.value )

        // Пауза в тиках
        m.putInt("impulse0timeImpPause", LiveData.impulse0timeImpPause.value )
        m.putInt("impulse1timeImpPause", LiveData.impulse1timeImpPause.value )

        println("ok")
    }



}
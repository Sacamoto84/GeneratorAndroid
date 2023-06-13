package com.example.generator2.model

import com.example.generator2.backup.MMKv
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

object LiveData {

        var ch1_EN          = MutableStateFlow(false)
        var ch1_Carrier_Filename = MutableStateFlow("Sine")
        var ch1_Carrier_Fr  = MutableStateFlow(400.0f)     //Частота несущей
        var ch1_AM_EN       = MutableStateFlow(false)
        var ch1_AM_Filename = MutableStateFlow("09_Ramp")
        var ch1_AM_Fr       = MutableStateFlow(8.7f)
        var ch1_FM_EN       = MutableStateFlow(false)
        var ch1_FM_Filename = MutableStateFlow("06_CHIRP")

        var ch1_FM_Dev      = MutableStateFlow(1100f)       //Частота базы
        var ch1_FM_Fr       = MutableStateFlow(5.1f)

        var ch2_EN          = MutableStateFlow(false)
        var ch2_Carrier_Filename = MutableStateFlow("Sine")
        var ch2_Carrier_Fr  = MutableStateFlow(2000.0f) //Частота несущей
        var ch2_AM_EN       = MutableStateFlow(false)
        var ch2_AM_Filename = MutableStateFlow("09_Ramp")
        var ch2_AM_Fr       = MutableStateFlow(8.7f)
        var ch2_FM_EN       = MutableStateFlow(false)
        var ch2_FM_Filename = MutableStateFlow("06_CHIRP")

        var ch2_FM_Dev      = MutableStateFlow(1100f) //Частота базы
        var ch2_FM_Fr       = MutableStateFlow(5.1f)

        var volume0 = MutableStateFlow(0.9f)  //Используется для AudioDevice = maxVolume0 * currentVolume0
        var volume1 = MutableStateFlow(0.9f)

        var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
        var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
        var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список

        var mono       = MutableStateFlow(false) //Режим повторения настроек второго канала с первым
        var invert     = MutableStateFlow(false) //Инверсия сигнала во втором канале, только при моно

        var shuffle    = MutableStateFlow(false) //меняем левый и правый канал в стерео режиме

        var enL  = MutableStateFlow(true)
        var enR  = MutableStateFlow(true)

        var maxVolume0 = MutableStateFlow(0.9f)    //JsonVolume максимальная громкость усилителя
        var maxVolume1 = MutableStateFlow(0.9f)

        var currentVolume0 = MutableStateFlow(1.0f) //Громкость канала на регуляторе 0 100 JsonConfig()
        var currentVolume1 = MutableStateFlow(1.0f)

        var ch1AmDepth = MutableStateFlow(1f) //Глубина AM модуляции
        var ch2AmDepth = MutableStateFlow(1f) //Глубина AM модуляции
        /**
         *  ### Импульсный режим
         */
        val impulse0 = MutableStateFlow(false)    //Импульсный режим канала
        val impulse1 = MutableStateFlow(false)

        // Ширина импульса в тиках
        val impulse0timeImp = MutableStateFlow(9)
        val impulse1timeImp = MutableStateFlow(9)

        // Пауза в тиках
        val impulse0timeImpPause = MutableStateFlow(9)
        val impulse1timeImpPause = MutableStateFlow(9)









}

private const val MAINFOLDER_NAME = "Gen3"
var MAINFOLDER: File? = null

val mmkv = MMKv()





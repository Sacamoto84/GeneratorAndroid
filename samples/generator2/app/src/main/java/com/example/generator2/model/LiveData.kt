package com.example.generator2.model

import androidx.lifecycle.LiveData
import com.example.generator2.backup.MMKv
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

data class dataLiveData (

        var ch1_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var ch1_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),
        var ch1_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(400.0f),    //Частота несущей
        var ch1_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var ch1_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),
        var ch1_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),
        var ch1_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var ch1_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"),

        var ch1_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),       //Частота базы
        var ch1_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),

        var ch2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var ch2_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),
        var ch2_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(2000.0f), //Частота несущей
        var ch2_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var ch2_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),
        var ch2_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),
        var ch2_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
        var ch2_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"),

        var ch2_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f), //Частота базы
        var ch2_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),

        var volume0: MutableStateFlow<Float> = MutableStateFlow(0.9f),  //Используется для AudioDevice = maxVolume0 * currentVolume0
        var volume1: MutableStateFlow<Float> = MutableStateFlow(0.9f),



        var mono: MutableStateFlow<Boolean> = MutableStateFlow(false), //Режим повторения настроек второго канала с первым
        var invert: MutableStateFlow<Boolean> = MutableStateFlow(false), //Инверсия сигнала во втором канале, только при моно

        var shuffle: MutableStateFlow<Boolean> = MutableStateFlow(false), //меняем левый и правый канал в стерео режиме

        var enL: MutableStateFlow<Boolean> = MutableStateFlow(true),
        var enR: MutableStateFlow<Boolean> = MutableStateFlow(true),

        var maxVolume0: MutableStateFlow<Float> = MutableStateFlow(0.9f),   //JsonVolume максимальная громкость усилителя
        var maxVolume1: MutableStateFlow<Float> = MutableStateFlow(0.9f),

        var currentVolume0: MutableStateFlow<Float> = MutableStateFlow(1.0f), //Громкость канала на регуляторе 0 100 JsonConfig()
        var currentVolume1: MutableStateFlow<Float> = MutableStateFlow(1.0f),

        var ch1AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f), //Глубина AM модуляции
        var ch2AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f), //Глубина AM модуляции
        /**
         *  ### Импульсный режим
         */
        val impulse0: MutableStateFlow<Boolean> = MutableStateFlow(false) ,   //Импульсный режим канала
        val impulse1: MutableStateFlow<Boolean> = MutableStateFlow(false),

        // Ширина импульса в тиках
        val impulse0timeImp: MutableStateFlow<Int> = MutableStateFlow(9),
        val impulse1timeImp: MutableStateFlow<Int> = MutableStateFlow(9),

        // Пауза в тиках
        val impulse0timeImpPause: MutableStateFlow<Int> = MutableStateFlow(9),
        val impulse1timeImpPause: MutableStateFlow<Int> = MutableStateFlow(9),

        //Количество звезд
        val star: MutableStateFlow<Int> = MutableStateFlow(0),

        //Имя текущего пресета
        val presetsName: MutableStateFlow<String> = MutableStateFlow("")


)

var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список


private const val MAINFOLDER_NAME = "Gen3"
var MAINFOLDER: File? = null

val mmkv = MMKv()

val LiveData : dataLiveData = dataLiveData()


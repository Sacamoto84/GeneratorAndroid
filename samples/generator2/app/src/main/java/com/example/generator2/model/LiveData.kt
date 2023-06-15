package com.example.generator2.model

import androidx.lifecycle.LiveData
import com.example.generator2.backup.MMKv
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

data class dataLiveData(

    var ch1_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),              //PR
    var ch1_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),//PR
    var ch1_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(400.0f),       //PR //Частота несущей
    var ch1_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR
    var ch1_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),  //PR
    var ch1_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),              //PR
    var ch1_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR
    var ch1_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"), //PR
    var ch1_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),            //PR //Частота базы
    var ch1_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),              //PR

    var ch2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
    var ch2_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"), //PR
    var ch2_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(2000.0f),    //PR Частота несущей
    var ch2_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR
    var ch2_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),//PR
    var ch2_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),            //PR
    var ch2_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR
    var ch2_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"),//PR
    var ch2_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),        //PR //Частота базы
    var ch2_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),          //PR

    var volume0: MutableStateFlow<Float> = MutableStateFlow(1f),              //PR Используется для AudioDevice = maxVolume0 * currentVolume0
    var volume1: MutableStateFlow<Float> = MutableStateFlow(1f),              //PR


    var mono: MutableStateFlow<Boolean> = MutableStateFlow(false),            //PR Режим повторения настроек второго канала с первым
    var invert: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR Инверсия сигнала во втором канале, только при моно

    var shuffle: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR меняем левый и правый канал в стерео режиме

    var enL: MutableStateFlow<Boolean> = MutableStateFlow(true),              //PR
    var enR: MutableStateFlow<Boolean> = MutableStateFlow(true),              //PR

    var maxVolume0: MutableStateFlow<Float> = MutableStateFlow(0.9f),         //PR JsonVolume максимальная громкость усилителя
    var maxVolume1: MutableStateFlow<Float> = MutableStateFlow(0.9f),         //PR

    var currentVolume0: MutableStateFlow<Float> = MutableStateFlow(1.0f),     //PR Громкость канала на регуляторе 0 100 JsonConfig()
    var currentVolume1: MutableStateFlow<Float> = MutableStateFlow(1.0f),     //PR

    var ch1AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR Глубина AM модуляции
    var ch2AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR Глубина AM модуляции
    /**
     *  ### Импульсный режим
     */
    val impulse0: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR Импульсный режим канала
    val impulse1: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR

    // Ширина импульса в тиках
    val impulse0timeImp: MutableStateFlow<Int> = MutableStateFlow(9),         //PR
    val impulse1timeImp: MutableStateFlow<Int> = MutableStateFlow(9),         //PR

    // Пауза в тиках
    val impulse0timeImpPause: MutableStateFlow<Int> = MutableStateFlow(9),    //PR
    val impulse1timeImpPause: MutableStateFlow<Int> = MutableStateFlow(9),    //PR

    //Количество звезд
    val star: MutableStateFlow<Int> = MutableStateFlow(0),                    //PR

    //Имя текущего пресета
    val presetsName: MutableStateFlow<String> = MutableStateFlow("")          //PR


)

var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список


private const val MAINFOLDER_NAME = "Gen3"
var MAINFOLDER: File? = null

val mmkv = MMKv()

val LiveData: dataLiveData = dataLiveData()


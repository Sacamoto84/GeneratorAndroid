package com.example.generator2.model

import androidx.compose.runtime.mutableIntStateOf
import com.example.generator2.backup.MMKv
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

data class dataLiveData(

    var ch1_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),              //PR PS PC
    var ch1_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),//PR PS PC
    var ch1_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(400.0f),       //PR PS PC //Частота несущей
    var ch1_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR PS PC
    var ch1_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),  //PR PS PC
    var ch1_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),              //PR PS PC
    var ch1_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR PS PC
    var ch1_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"), //PR PS PC
    var ch1_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),            //PR PS PC //Частота базы
    var ch1_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),              //PR PS PC

    var ch2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
    var ch2_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"), //PR PS PC
    var ch2_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(2000.0f),     //PR PS PC Частота несущей
    var ch2_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC
    var ch2_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"), //PR PS PC
    var ch2_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),             //PR PS PC
    var ch2_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC
    var ch2_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"),//PR PS PC
    var ch2_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),           //PR PS PC Частота базы
    var ch2_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),             //PR PS PC

    var volume0: MutableStateFlow<Float> = MutableStateFlow(1f),              //PR PS PC Используется для AudioDevice = maxVolume0 * currentVolume0
    var volume1: MutableStateFlow<Float> = MutableStateFlow(1f),              //PR PS PC


    var mono: MutableStateFlow<Boolean> = MutableStateFlow(false),            //PR PS PC Режим повторения настроек второго канала с первым
    var invert: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC Инверсия сигнала во втором канале, только при моно

    var shuffle: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR PS PC меняем левый и правый канал в стерео режиме

    var enL: MutableStateFlow<Boolean> = MutableStateFlow(true),              //PR PS PC
    var enR: MutableStateFlow<Boolean> = MutableStateFlow(true),              //PR PS PC

    var maxVolume0: MutableStateFlow<Float> = MutableStateFlow(0.9f),         //PR PS PC JsonVolume максимальная громкость усилителя
    var maxVolume1: MutableStateFlow<Float> = MutableStateFlow(0.9f),         //PR PS PC

    var currentVolume0: MutableStateFlow<Float> = MutableStateFlow(1.0f),     //PR PS PC Громкость канала на регуляторе 0 100 JsonConfig()
    var currentVolume1: MutableStateFlow<Float> = MutableStateFlow(1.0f),     //PR PS PC

    var ch1AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC Глубина AM модуляции
    var ch2AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC Глубина AM модуляции
    /**
     *  ### Импульсный режим
     */
    val impulse0: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR PS PC Импульсный режим канала
    val impulse1: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR PS PC

    // Ширина импульса в тиках
    val impulse0timeImp: MutableStateFlow<Int> = MutableStateFlow(9),         //PR PS PC
    val impulse1timeImp: MutableStateFlow<Int> = MutableStateFlow(9),         //PR PS PC

    // Пауза в тиках
    val impulse0timeImpPause: MutableStateFlow<Int> = MutableStateFlow(9),    //PR PS PC
    val impulse1timeImpPause: MutableStateFlow<Int> = MutableStateFlow(9),    //PR PS PC

    //Количество звезд
    val star: MutableStateFlow<Int> = MutableStateFlow(0),                    //PR PS PC

    //Имя текущего пресета
    val presetsName: MutableStateFlow<String> = MutableStateFlow(""),         //PR PS(name) PC

    val parameterFloat0: MutableStateFlow<Float> = MutableStateFlow(1.0f), //PR PS PC 50 Импульс CH1 время действия импульса 1 сек
    val parameterFloat1: MutableStateFlow<Float> = MutableStateFlow(1.0f), //PR PS PC
    val parameterFloat2: MutableStateFlow<Float> = MutableStateFlow(0.0f), //PR PS PC
    val parameterFloat3: MutableStateFlow<Float> = MutableStateFlow(0.0f), //PR PS PC
    val parameterFloat4: MutableStateFlow<Float> = MutableStateFlow(0.0f), //PR PS PC
    val parameterFloat5: MutableStateFlow<Float> = MutableStateFlow(0.0f), //PR PS PC
    val parameterFloat6: MutableStateFlow<Float> = MutableStateFlow(0.0f), //PR PS PC
    val parameterFloat7: MutableStateFlow<Float> = MutableStateFlow(0.0f), //PR PS PC

    val parameterInt0: MutableStateFlow<Int> = MutableStateFlow(1), //PR PS PC 50 Импульс CH1 Частота от 1Гц до 50Гц
    val parameterInt1: MutableStateFlow<Int> = MutableStateFlow(1), //PR PS PC
    val parameterInt2: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC 50 Импульс CH1 Fire
    val parameterInt3: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt4: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt5: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt6: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt7: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC

)

var m4recompose = mutableIntStateOf(0) //Для обновления главного экрана

var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список


private const val MAINFOLDER_NAME = "Gen3"
var MAINFOLDER: File? = null

val mmkv = MMKv()

val LiveData: dataLiveData = dataLiveData()


package com.example.generator2.features.generator

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
fun observe(gen: Generator) {
    Timber.i("observe()-------------------------------------------------------------- Start")
    val dispatchers = Dispatchers.IO
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Carrier_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.CR, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Carrier_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.CR, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_AM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.AM, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_AM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.AM, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_FM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH0, GeneratorMOD.FM, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_FM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CH1, GeneratorMOD.FM, it, gen ) } }
    Timber.i("observe()-------------------------------------------------------------- End")
}
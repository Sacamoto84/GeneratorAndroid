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

    //Любой параметр, влияющий на буфер FM, требует его пересчёта
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_FM_Dev.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_FM_Dev.collect { gen.updateFm(1) } }

    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Carrier_Fr.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Carrier_Fr.collect { gen.updateFm(1) } }

    GlobalScope.launch(dispatchers) { gen.liveData.ch1FmMin.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch1FmMax.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2FmMin.collect { gen.updateFm(1) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2FmMax.collect { gen.updateFm(1) } }

    //Переключение режима задания частот FM (0 - несущая ± девиация, 1 - min/max)
    GlobalScope.launch(dispatchers) { gen.liveData.parameterInt0.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.parameterInt1.collect { gen.updateFm(1) } }


    Timber.i("observe()-------------------------------------------------------------- End")
}
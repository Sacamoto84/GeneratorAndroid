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
    GlobalScope.launch(dispatchers) { gen.liveData.chL_Carrier_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.CR, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_Carrier_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.CR, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chL_AM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.AM, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_AM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.AM, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chL_Master_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.MASTER, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_Master_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.MASTER, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chL_FM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.FM, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_FM_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.FM, it, gen ) } }

    GlobalScope.launch(dispatchers) { gen.liveData.chL_Morph_Slot0_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.MORPH0, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chL_Morph_Slot1_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.MORPH1, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chL_Morph_Slot2_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHL, GeneratorMOD.MORPH2, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_Morph_Slot0_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.MORPH0, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_Morph_Slot1_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.MORPH1, it, gen ) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_Morph_Slot2_Filename.collect { Spinner_Send_Buffer( GeneratorCH.CHR, GeneratorMOD.MORPH2, it, gen ) } }

    //Любой параметр, влияющий на буфер FM, требует его пересчёта
    GlobalScope.launch(dispatchers) { gen.liveData.chL_FM_Dev.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_FM_Dev.collect { gen.updateFm(1) } }

    GlobalScope.launch(dispatchers) { gen.liveData.chL_Carrier_Fr.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chR_Carrier_Fr.collect { gen.updateFm(1) } }

    GlobalScope.launch(dispatchers) { gen.liveData.chLFmMin.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chLFmMax.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chRFmMin.collect { gen.updateFm(1) } }
    GlobalScope.launch(dispatchers) { gen.liveData.chRFmMax.collect { gen.updateFm(1) } }

    //Переключение режима задания частот FM (0 - несущая ± девиация, 1 - min/max)
    GlobalScope.launch(dispatchers) { gen.liveData.parameterInt0.collect { gen.updateFm(0) } }
    GlobalScope.launch(dispatchers) { gen.liveData.parameterInt1.collect { gen.updateFm(1) } }


    Timber.i("observe()-------------------------------------------------------------- End")
}
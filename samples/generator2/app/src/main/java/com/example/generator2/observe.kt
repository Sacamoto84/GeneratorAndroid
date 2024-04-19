package com.example.generator2

import com.example.generator2.features.generator.Generator
import com.example.generator2.util.UtilsKT
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
fun observe(utils: UtilsKT, gen: Generator) {

    Timber.i("observe()-------------------------------------------------------------- Start")
    val dispatchers = Dispatchers.IO

    //
    GlobalScope.launch(dispatchers) {
        gen.liveData.ch1_Carrier_Filename.collect {
            utils.Spinner_Send_Buffer(
                "CH0",
                "CR",
                it,
                gen
            )
        }
    }
    GlobalScope.launch(dispatchers) {
        gen.liveData.ch2_Carrier_Filename.collect {
            utils.Spinner_Send_Buffer(
                "CH1",
                "CR",
                it,
                gen
            )
        }
    }
    //
    GlobalScope.launch(dispatchers) {
        gen.liveData.ch1_AM_Filename.collect {
            utils.Spinner_Send_Buffer(
                "CH0",
                "AM",
                it,
                gen
            )
        }
    }
    GlobalScope.launch(dispatchers) {
        gen.liveData.ch2_AM_Filename.collect {
            utils.Spinner_Send_Buffer(
                "CH1",
                "AM",
                it,
                gen
            )
        }
    }
    //
    GlobalScope.launch(dispatchers) {
        gen.liveData.ch1_FM_Filename.collect {
            utils.Spinner_Send_Buffer(
                "CH0",
                "FM",
                it,
                gen
            )
        }
    }
    GlobalScope.launch(dispatchers) {
        gen.liveData.ch2_FM_Filename.collect {
            utils.Spinner_Send_Buffer(
                "CH1",
                "FM",
                it,
                gen
            )
        }
    }
    //

    Timber.i("observe()-------------------------------------------------------------- End")

}
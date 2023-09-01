package com.example.generator2

import com.example.generator2.di.Hub
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
fun observe(hub: Hub) {
    Timber.i("observe()-------------------------------------------------------------- Start")
    val dispatchers = Dispatchers.IO

    //
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_Carrier_Filename.collect { hub.utils.Spinner_Send_Buffer("CH0", "CR", it) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_Carrier_Filename.collect { hub.utils.Spinner_Send_Buffer("CH1","CR", it ) } }
    //
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_AM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH0", "AM", it) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_AM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH1","AM", it ) } }
    //
    GlobalScope.launch(dispatchers) { gen.liveData.ch1_FM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH0", "FM", it) } }
    GlobalScope.launch(dispatchers) { gen.liveData.ch2_FM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH1","FM", it ) } }
    //

    Timber.i("observe()-------------------------------------------------------------- End")

}
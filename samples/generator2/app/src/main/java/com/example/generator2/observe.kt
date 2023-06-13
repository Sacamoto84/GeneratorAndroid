package com.example.generator2

import com.example.generator2.di.Hub
import com.example.generator2.model.LiveData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
fun observe(hub: Hub) {
    Timber.i("observe()-------------------------------------------------------------- Start")
    val dispatchers = Dispatchers.IO

    GlobalScope.launch(dispatchers) { LiveData.volume0.collect {hub.playbackEngine.setVolume(0, it ) } }
    GlobalScope.launch(dispatchers) { LiveData.volume1.collect {hub.playbackEngine.setVolume(1, it ) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_EN.collect {hub.playbackEngine.setEN(0, it ) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_EN.collect {hub.playbackEngine.setEN(1, it ) } }

    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_AM_EN.collect {hub.playbackEngine.setAM_EN(0, it ) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_AM_EN.collect {hub.playbackEngine.setAM_EN(1, it ) } }

    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_FM_EN.collect {hub.playbackEngine.setFM_EN(0, it) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_FM_EN.collect {hub.playbackEngine.setFM_EN(1, it ) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_Carrier_Fr.collect {hub.playbackEngine.setCarrier_fr(0, it.toInt().toFloat()) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_Carrier_Fr.collect {hub.playbackEngine.setCarrier_fr(1, it.toInt().toFloat() ) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_AM_Fr.collect {hub.playbackEngine.setAM_fr(0, it) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_AM_Fr.collect {hub.playbackEngine.setAM_fr(1, it ) } }

    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_FM_Dev.collect {hub.playbackEngine.setFM_Dev(0, it.toInt().toFloat()) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_FM_Dev.collect {hub.playbackEngine.setFM_Dev(1, it.toInt().toFloat()) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_FM_Fr.collect { hub.playbackEngine.setFM_fr(0, it ) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_FM_Fr.collect { hub.playbackEngine.setFM_fr(1, it ) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_Carrier_Filename.collect { hub.utils.Spinner_Send_Buffer("CH0", "CR", it) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_Carrier_Filename.collect { hub.utils.Spinner_Send_Buffer("CH1","CR", it ) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_AM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH0", "AM", it) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_AM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH1","AM", it ) } }
    //
    GlobalScope.launch(dispatchers) { LiveData.ch1_FM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH0", "FM", it) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2_FM_Filename.collect {hub.utils.Spinner_Send_Buffer("CH1","FM", it ) } }
    //

    //Включение моно в самом драйвере Oboe, второй канал игнорируется
    GlobalScope.launch(dispatchers) { LiveData.mono.collect { hub.audioDevice.playbackEngine.setMono(it) } }

    GlobalScope.launch(dispatchers) { LiveData.invert.collect { hub.audioDevice.playbackEngine.setInvertPhase(it) } }

    GlobalScope.launch(dispatchers) { LiveData.enL.collect { hub.audioDevice.playbackEngine.setEnL(it) } }
    GlobalScope.launch(dispatchers) { LiveData.enR.collect { hub.audioDevice.playbackEngine.setEnR(it) } }
    GlobalScope.launch(dispatchers) { LiveData.shuffle.collect {hub.audioDevice.playbackEngine.setShuffle(it) } }

    GlobalScope.launch(dispatchers) { LiveData.ch1AmDepth.collect {hub.playbackEngine.setAmDepth(0, it) } }
    GlobalScope.launch(dispatchers) { LiveData.ch2AmDepth.collect {hub.playbackEngine.setAmDepth(1, it) } }

    /**
     * Импульсный режим
     */

    GlobalScope.launch(dispatchers) { LiveData.impulse0.collect {hub.playbackEngine.setImpulse(0, it) } }
    GlobalScope.launch(dispatchers) { LiveData.impulse1.collect {hub.playbackEngine.setImpulse(1, it) } }

    // Установка ширины импульса
    GlobalScope.launch(dispatchers) { LiveData.impulse0timeImp.collect {hub.playbackEngine.setImpulseWidthTime(0, it) } }
    GlobalScope.launch(dispatchers) { LiveData.impulse1timeImp.collect {hub.playbackEngine.setImpulseWidthTime(1, it) } }

    // Установка паузы импульса
    GlobalScope.launch(dispatchers) { LiveData.impulse0timeImpPause.collect {hub.playbackEngine.setImpulsePauseTime(0, it) } }
    GlobalScope.launch(dispatchers) { LiveData.impulse1timeImpPause.collect {hub.playbackEngine.setImpulsePauseTime(1, it) } }



    Timber.i("observe()-------------------------------------------------------------- End")

}
package com.example.generator2.audio

import com.example.generator2.generator.generatorRenderAudio
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


val audioMixerPump = AudioMixerPump()

class AudioMixerPump {


   val bufferSizeGenDefault = 8192 //размер буфера по умолчанию для генератора
   var bufferSize : Int = bufferSizeGenDefault //Текущий размер буфера который берется от размера буфера от плеера

    init {
        Timber.i("Запуск AudioMixerPump ")
        run()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun run()
    {
        GlobalScope.launch(Dispatchers.IO) {
            while (true)
            {

                

               val bugGen = generatorRenderAudio(bufferSize)




               chAudioOut.send(bugGen)
            }
        }
    }

}
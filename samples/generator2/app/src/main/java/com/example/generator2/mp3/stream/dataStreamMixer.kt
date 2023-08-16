package com.example.generator2.mp3.stream

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//val channelDataStreamOutAudioProcessor =
//val channelDataStreamOutGenerator =
//val channelDataStreamOutMixer

@OptIn(DelicateCoroutinesApi::class)
fun dataStreamMixer()
{

    GlobalScope.launch(Dispatchers.IO) {

        //val bufGen = channelDataStreamOutGenerator.tryReceive()
        //val bufMp3 = channelDataStreamOutAudioProcessor.receive()

    }

}





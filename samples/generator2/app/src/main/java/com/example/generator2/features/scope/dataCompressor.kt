package com.example.generator2.features.scope

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//@OptIn(DelicateCoroutinesApi::class)
//fun dataCompressor(scope: Scope) {
//
//    GlobalScope.launch(Dispatchers.IO) {
//
//        while (true) {
//
//            val buf = scope.channelAudioOut.receive()
//
//            //Передаем FFT порцию данных
//            //Spectrogram.sentToFloatRingBufferFFT(buf, buf.size, scope.audioSampleRate)
//
//            NativeFloatDirectBuffer.add(buf, buf.size, scope.compressorCount.floatValue.toInt())
//
//            scope.deferredOscill.send(0)
//            scope.deferredLissagu.send(0)
//        }
//    }
//}
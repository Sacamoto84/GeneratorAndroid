package com.example.generator2.features.scope

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor(scope: Scope) {

    GlobalScope.launch(Dispatchers.IO) {

        while (true) {

            val buf = scope.channelAudioOut.receive()

            //Передаем FFT порцию данных
            //Spectrogram.sentToFloatRingBufferFFT(buf, buf.size, scope.audioSampleRate)

            NativeFloatDirectBuffer.add(buf, buf.size, scope.compressorCount.floatValue.toInt())
            scope.deferred.complete(0)
        }
    }
}


class FloatRingBuffer(val entrySize: Int, val bufferSize: Int = 256) {
    val buffer = FloatArray(entrySize * bufferSize)
    private var start = 0
    private var end = 0
    private var isFull = false

    fun add(entry: FloatArray) {
        require(entry.size == entrySize) { "Entry size must be $entrySize" }

        // Записываем новую запись в буфер
        //entry.copyInto(buffer, end * entrySize)

        System.arraycopy(entry, 0, buffer, end * entrySize, entrySize)

        end = (end + 1) % bufferSize

        // Проверяем, если буфер заполнен, двигаем старт
        if (isFull) {
            start = (start + 1) % bufferSize
        } else if (end == start) {
            isFull = true
        }
    }

    fun toExternalFloatArray(result: FloatArray) {
        if (isFull) {
            // Копируем данные из двух частей буфера
            val part1Size = (bufferSize - start) * entrySize
            System.arraycopy(buffer, start * entrySize, result, 0, part1Size)
            System.arraycopy(buffer, 0, result, part1Size, end * entrySize)
        } else {
            // Копируем данные из одной части буфера
            System.arraycopy(buffer, 0, result, 0, end * entrySize)
        }
    }
}
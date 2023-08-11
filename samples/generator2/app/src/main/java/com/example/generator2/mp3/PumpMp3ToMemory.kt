package com.example.generator2.mp3

import android.net.Uri
import c.ponom.audiuostreams.audiostreams.AudioFileSoundStream
import c.ponom.audiuostreams.audiostreams.StreamPump
import com.example.generator2.AppPath
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class PumpMp3ToMemory(val uri: Uri, val onDone: (ShortBuffer) -> Unit = {}) {

    var isEnded = false

    lateinit var audioPump: StreamPump

    var buffer : ShortBuffer = ShortBuffer.allocate(0)

    //val audioOut = AudioTrackToMemory(audioIn.sampleRate, audioIn.channelsCount)

    fun readBuffer():ShortBuffer
    {
        return buffer
    }


    fun run() {

        val audioIn = AudioFileSoundStream(AppPath().download + "/1.mp3")

        val format = audioIn.format

        val size = convertUsToShortSize(
            format.getLong("durationUs").div(1000),
            format.getInteger("sample-rate"),
            format.getInteger("channel-count")
        )

        buffer = ShortBuffer.allocate(size.toInt())

        val datetime = System.currentTimeMillis()
        var count = 0L
        audioPump = StreamPump(audioIn, null, 8192,

            onEachPumpShort = {
                //println(buffer.position())
                buffer.put(it)
                // recordLevel.postValue(SoundVolumeUtils.getRMSVolume(ArrayUtils.byteToShortArrayLittleEndian(it)).toFloat())
            },
            onWrite = {
                count = it
            },
            onFinish = {

                println("End Position > " + buffer.position())
                println("End capacity > " + buffer.capacity())
                println("End limit > " + buffer.limit())
                println("End count > $count")

                buffer.flip() //Перевод на чтение

                val now = System.currentTimeMillis() - datetime

                Timber.e("-----------------------------")
                Timber.e("| Окончание воспроизведения")
                Timber.e("| Время воспроизведения $now ms")
                Timber.e("-----------------------------")

                isEnded = true
                //audioPump.stop(true)
                onDone(buffer)
            },
            onFatalError = {
                Timber.tag(TAG).e("Error=%s", it.localizedMessage)
            })

        audioPump.start(true)
    }

    fun stop() {
        audioPump.stop(true)
    }

    private fun convertUsToShortSize(
        ms: Long,
        sampleRate: Int = 48000,
        channelsCount: Int = 2
    ): Long {
        val impInMs = sampleRate.div(1000)
        return ms * impInMs * channelsCount + 2_000_000
    }

}
package com.example.generator2.generator

import android.media.AudioFormat
import c.ponom.audiuostreams.audiostreams.AudioTrackOutputStream
import c.ponom.audiuostreams.audiostreams.StreamPump
import timber.log.Timber


const val sampleRate = 192000

fun generatorRun()
{
    val audioInStream: GeneratorInputStream
    val audioOutStream: AudioTrackOutputStream

    try {
        audioInStream = GeneratorInputStream(sampleRate)
        audioOutStream = AudioTrackOutputStream(sampleRate, 2, 100, AudioFormat.ENCODING_PCM_16BIT)
    } catch (e: Exception) {
        return
    }

    val audioPump = StreamPump(audioInStream, audioOutStream, 12000,

        onWrite = {
            //Timber.i("*............................................")
        },
        onFinish = {
            //Timber.i("onFinish........................................")
        },
        onFatalError = {
            //Timber.i("onFatalError........................................")
        }
    )

    Timber.i("play: AudioTrackOutputStream buffer =" + "${audioOutStream?.audioOut?.bufferSizeInFrames} frames")
    audioPump.start(false)
    audioOutStream.play()

}
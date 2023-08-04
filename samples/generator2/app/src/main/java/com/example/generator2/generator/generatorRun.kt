package com.example.generator2.generator

import c.ponom.audiuostreams.audiostreams.AudioTrackOutputStream
import c.ponom.audiuostreams.audiostreams.StreamPump
import timber.log.Timber

fun generatorRun()
{
    val audioInStream: GeneratorInputStream
    val audioOutStream: AudioTrackOutputStream

    try {
        audioInStream = GeneratorInputStream()
        audioOutStream = GeneratorAudioOut
    } catch (e: Exception) {
        return
    }

    val audioPump = StreamPump(audioInStream, audioOutStream, 48000,

        onWrite = {
            Timber.i("*............................................")
        },
        onFinish = {
            Timber.i("onFinish........................................")
        },
        onFatalError = {
            Timber.i("onFatalError........................................")
        }
    )

    Timber.i("play: AudioTrackOutputStream buffer =" + "${audioOutStream?.audioOut?.bufferSizeInFrames} frames")
    audioPump.start(false)
    audioOutStream.play()

}
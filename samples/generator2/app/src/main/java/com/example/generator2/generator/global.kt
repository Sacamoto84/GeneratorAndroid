package com.example.generator2.generator

import android.media.AudioFormat
import c.ponom.audiuostreams.audiostreams.AudioTrackOutputStream

val GeneratorAudioOut = AudioTrackOutputStream(48000,2,0, AudioFormat.ENCODING_PCM_FLOAT)


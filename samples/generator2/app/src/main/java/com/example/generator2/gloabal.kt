package com.example.generator2

import android.media.AudioFormat
import com.example.generator2.audio.AudioOut
import com.example.generator2.backup.MMKv

//lateinit var exoplayer: PlayerMP3

//val gen = Generator()

var audioOut: AudioOut = AudioOut(48000, 200, AudioFormat.ENCODING_PCM_FLOAT)

//val audioMixerPump = AudioMixerPump(gen)

val mmkv = MMKv()

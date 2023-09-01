package com.example.generator2

import android.media.AudioFormat
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.audio.AudioOut
import com.example.generator2.backup.MMKv
import com.example.generator2.generator.Generator
import com.example.generator2.mp3.PlayerMP3
import com.example.generator2.scope.Scope


val mmkv = MMKv()


lateinit var exoplayer: PlayerMP3

val gen = Generator()

val audioMixerPump = AudioMixerPump()

var audioOut : AudioOut = AudioOut(48000,200, AudioFormat.ENCODING_PCM_FLOAT)

val scope = Scope()




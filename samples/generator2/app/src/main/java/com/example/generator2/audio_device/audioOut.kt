package com.example.generator2.audio_device

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager


fun audioOutSpeaker(context : Context) {
     val localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioOutWired(context)
    localAudioManager.setSpeakerphoneOn(true)

}

fun audioOutWired(context : Context) {
    val localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    localAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
    localAudioManager.stopBluetoothSco()
    localAudioManager.setBluetoothScoOn(false)

    localAudioManager.setSpeakerphoneOn(false)
}

fun audioOutBT( context : Context) {
    val localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    localAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
    localAudioManager.startBluetoothSco()
    localAudioManager.setBluetoothScoOn(true)
}

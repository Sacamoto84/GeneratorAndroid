package com.example.generator2.audio_device

import android.content.Context
import android.media.AudioManager
import timber.log.Timber


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

fun getCurrentAudioDevices(context : Context)
{
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val a = audioManager.isSpeakerphoneOn()
    val b = audioManager.isBluetoothScoOn()
    val c = audioManager.isBluetoothA2dpOn()
    val d = audioManager.isWiredHeadsetOn()

    // Вывод информации о текущем устройстве вывода
    Timber.d("Текущее устройство вывода: | Speaker $a | BTSco: $b | A2Dp: $c | Wired: $d")

}






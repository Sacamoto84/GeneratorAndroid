package com.example.generator2.features.audio

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import timber.log.Timber


fun audioOutSpeaker(context: Context) {
    val localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioOutWired(context)
    localAudioManager.setSpeakerphoneOn(true)
}

fun audioOutWired(context: Context) {
    val localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    //localAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
    //localAudioManager.setBluetoothScoOn(false)
    //localAudioManager.stopBluetoothSco()
    localAudioManager.setSpeakerphoneOn(false)
}

fun audioOutBT(context: Context) {

    val localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    //localAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)

    localAudioManager.setBluetoothScoOn(true)
    localAudioManager.startBluetoothSco()
    localAudioManager.setBluetoothA2dpOn(true)

    //localAudioManager.setMode(AudioManager.MODE_RINGTONE)

    localAudioManager.setSpeakerphoneOn(false)

    Timber.e("Bluetooth Headset On " + localAudioManager.getMode())
    Timber.e("A2DP: " + localAudioManager.isBluetoothA2dpOn() + ". SCO: " + localAudioManager.isBluetoothScoAvailableOffCall());

    //GlobalScope.launch (Dispatchers.IO){
    //   btOff()
    //   btOn()
    //}

    localAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)
}

fun getCurrentAudioDevices(context: Context): String {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val a = audioManager.isSpeakerphoneOn()
    val b = audioManager.isBluetoothScoOn()
    //val c = audioManager.isBluetoothA2dpOn()
    val d = audioManager.isWiredHeadsetOn()

    // Вывод информации о текущем устройстве вывода
    //Timber.d("Текущее устройство вывода: | Speaker $a | BTSco: $b | | Wired: $d")

    var res = "Auto select"

    if (a) res = "built-in speaker"
    if (b) res = "A2DP"
    if (d) res = "headphones"

    return res
}


fun btOn() {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


    if (bluetoothAdapter != null) {
        if (bluetoothAdapter.isEnabled()) {
            // Bluetooth включен
        } else {
            // Bluetooth выключен
            bluetoothAdapter?.enable()
        }
    } else {
        // Bluetooth не поддерживается на этом устройстве
    }

}

fun btOff() {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()



    if (bluetoothAdapter != null) {

        if (bluetoothAdapter.isEnabled()) {
            // Bluetooth включен
            bluetoothAdapter?.disable()
        } else {
            // Bluetooth выключен
        }

    } else {
        // Bluetooth не поддерживается на этом устройстве
    }


}



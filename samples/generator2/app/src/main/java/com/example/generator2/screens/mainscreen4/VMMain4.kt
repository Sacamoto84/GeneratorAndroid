package com.example.generator2.screens.mainscreen4

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.example.generator2.AppPath
import com.example.generator2.element.Console2
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.update.Update
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VMMain4 @OptIn(UnstableApi::class) @Inject constructor(
    //@ApplicationContext contextActivity: Context,

    val audioMixerPump: AudioMixerPump,

    val update: Update,
    val appPath: AppPath
) : ViewModel() {

    val consoleLog = Console2()

    init {

        Timber.i("VMMain4 init{} start")

        consoleLog.println("")

        Timber.i("VMMain4 init{} end")

        //Spectrogram.setProcessorFFT(4096)

    }


}

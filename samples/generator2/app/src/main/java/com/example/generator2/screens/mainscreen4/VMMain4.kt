package com.example.generator2.screens.mainscreen4

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.generator2.AppPath
import com.example.generator2.Spectrogram
import com.example.generator2.di.MainAudioMixerPump
import com.example.generator2.element.Console2
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.update.Update
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

//@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VMMain4 @OptIn(UnstableApi::class) @Inject constructor(
    //@ApplicationContext contextActivity: Context,

    @MainAudioMixerPump
    val audioMixerPump: AudioMixerPump,

    val update: Update,
    val appPath: AppPath
) : ViewModel() {

    val consoleLog = Console2()

    init {

        Timber.i("VMMain4 init{} start")

        //keyboard = ScriptKeyboard(script)
        consoleLog.println("")
        //observe()

        //print("unit5Load()..")

        //hub.script.unit5Load() //Загрузить тест
        println("OK")
        print("launchScriptScope()..")
        //launchScriptScope() //Запуск скриптового потока
        println("OK")

//        while (!isInitialized) {
//            Timber.i("Ждем окончания инициализации")
//        }

        Timber.i("VMMain4 init{} end")

        //Spectrogram.setProcessorFFT(4096)

    }


    ////////////////////////////////////////////////////////
    private fun launchScriptScope() {

        println("global launchScriptScope()")

        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                //hub.script.run()
                delay(10)
            }
        }

        viewModelScope.launch(Dispatchers.Main) {

//            while (true) {
//                if ( hub.audioDevice.playbackEngine.getNeedAllData() == 1)
//                {
//                    //hub.audioDevice.playbackEngine.resetNeedAllData()
//                    delay(200)
//                    println("Global Отсылаем все данные")
//                    withContext(Dispatchers.IO)
//                    {
//                        hub.audioDevice.sendAlltoGen()
//                        hub.audioDevice.getDeviceId()
//                    }
//
//                }
//                delay(1000)
//            }
        }

    }

}

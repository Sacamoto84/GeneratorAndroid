package com.example.generator2.screens.mainscreen4

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.generator2.di.Hub
import com.example.generator2.element.Console2
import com.example.generator2.isInitialized
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

//@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VMMain4 @Inject constructor(
    @ApplicationContext contextActivity: Context,
    val hub : Hub
) : ViewModel(){

    val consoleLog = Console2()

    init {

        Timber.i("VMMain4 init{} start")

        //keyboard = ScriptKeyboard(script)
        consoleLog.println("")
        //observe()

        print("unit5Load()..")

        hub.script.unit5Load() //Загрузить тест
        println("OK")
        print("launchScriptScope()..")
        launchScriptScope() //Запуск скриптового потока
        println("OK")

        while(!isInitialized)
        {
            Timber.i("Ждем окончания инициализации")
        }

        Timber.i("VMMain4 init{} end")
    }



    ////////////////////////////////////////////////////////
    private fun launchScriptScope() {

        println("global launchScriptScope()")

        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                hub.script.run()
                delay(10)
            }
        }

        viewModelScope.launch(Dispatchers.Main) {

            while (true) {
                if ( hub.audioDevice.playbackEngine.getNeedAllData() == 1) {
                    hub.audioDevice.playbackEngine.resetNeedAllData()
                    delay(200)
                    println("Global Отсылаем все данные")
                    withContext(Dispatchers.IO)
                    {
                        hub.audioDevice.sendAlltoGen()
                        hub.audioDevice.getDeviceId()
                    }

                }
                delay(1000)
            }
        }

    }

}

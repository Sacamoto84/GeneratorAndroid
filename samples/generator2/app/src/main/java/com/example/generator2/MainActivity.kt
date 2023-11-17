package com.example.generator2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.pufferdb.android.AndroidPufferDB
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.generator.Generator
import com.example.generator2.presets.presetsInit
import com.example.generator2.presets.presetsSaveFile
import com.example.generator2.scope.Scope
import com.example.generator2.theme.Generator2Theme
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.update.Update
import com.example.generator2.update.kDownloader
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.libs.KeepScreenOn
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kdownloader.KDownloader
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

//AppMetrica.reportEvent('Button clicked!', '{}');

//String eventParameters = "{\"name\":\"Alice\", \"age\":\"18\"}";
//YandexMetrica.reportEvent("New person", eventParameters);

//YandexMetrica.reportEvent("Updates installed");

//try {
//    Integer.valueOf("00xffWr0ng");
//} catch (Throwable error) {
//    YandexMetrica.reportError("Error while parsing some integer number", error);
//}

//YandexMetrica.reportError(String message, Throwable error)
//YandexMetrica.reportError(String groupIdentifier, String message)
//YandexMetrica.reportError(String groupIdentifier, String message, Throwable error)

val API_key = "5ca5814f-74a8-46c1-ab17-da3101e88888"

@Singleton
@AndroidEntryPoint
@androidx.media3.common.util.UnstableApi
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var audioMixerPump: AudioMixerPump

    @Inject
    lateinit var gen: Generator

    @Inject
    lateinit var utils: UtilsKT

    @Inject
    lateinit var scope: Scope

    override fun onPause() {
        presetsSaveFile("default", AppPath().config, gen)
        //R.drawable.add
        super.onPause()
        println("...................onPause")
        //exitProcess(0)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())
        Timber.i("..................................onCreate.................................")

        GlobalScope.launch(Dispatchers.IO) {
            //delay(5000)
            Timber.w("Запуск Yandex Metrika")
            val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build()
            YandexMetrica.activate(applicationContext, config)
            YandexMetrica.enableActivityAutoTracking(application)
            YandexMetrica.reportEvent("Запуск")
        }

        gen

        kDownloader = KDownloader.create(applicationContext)

        AndroidPufferDB.init(applicationContext)

        presetsInit()

        initialization(applicationContext, gen, utils)

        audioOut

        audioMixerPump

        scope

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            customTypeface = resources.getFont(R.font.jetbrains)
//        }

        Utils.ContextMainActivity = applicationContext

        //play()


        //GlobalScope.launch(Dispatchers.IO) {
        //player.playUri()
        //exoplayer = PlayerMP3(applicationContext)
        //}


        setContent {

            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(colorDarkBackground, darkIcons = false)
            }

            KeepScreenOn()

            //initialState - С какого экрана переход
            //targetState   -переходит на
            //enterTransition - управляет тем, что EnterTransition выполняется, когда targetState  NavBackStackEntry на экране появляется значок .
            //exitTransition  - управляет тем, что ExitTransition  запускается, когда initialState NavBackStackEntry исчезает с экрана.
            Generator2Theme {
                Timber.i("..................................Generator2Theme.................................")

                   Update.run(applicationContext)
                   Navigation()
            }
        }
    }
}


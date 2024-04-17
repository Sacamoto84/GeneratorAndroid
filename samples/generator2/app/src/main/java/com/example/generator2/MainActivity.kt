package com.example.generator2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import com.bugsnag.android.Bugsnag
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.generator.Generator
import com.example.generator2.features.presets.presetsSaveFile
import com.example.generator2.features.scope.Scope
import com.example.generator2.theme.Generator2Theme
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.KeepScreenOn
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
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

    @Inject
    lateinit var appPath: AppPath


    override fun onPause() {
        presetsSaveFile("default", appPath.config, gen)
        //R.drawable.add
        super.onPause()
        println("...................onPause")
        //exitProcess(0)
    }


    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())
        Timber.i("..................................onCreate.................................")

        Utils.ContextMainActivity = applicationContext

        //play()


        //GlobalScope.launch(Dispatchers.IO) {
        //player.playUri()
        //exoplayer = PlayerMP3(applicationContext)
        //}

        Bugsnag.notify(RuntimeException("Test error"))

        //var a = 5
        //a = a/0

        setContent {

            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(colorDarkBackground, darkIcons = false)
            }

            //KeepScreenOn()

            //initialState - С какого экрана переход
            //targetState   -переходит на
            //enterTransition - управляет тем, что EnterTransition выполняется, когда targetState  NavBackStackEntry на экране появляется значок .
            //exitTransition  - управляет тем, что ExitTransition  запускается, когда initialState NavBackStackEntry исчезает с экрана.
            Generator2Theme {
                Timber.i("..................................Generator2Theme.................................")
                Navigation()
            }
        }
    }
}


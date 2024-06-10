package com.example.generator2

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.generator2.di.MainAudioMixerPump
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.presets.presetsSaveFile
import com.example.generator2.features.scope.Scope
import com.example.generator2.theme.Generator2Theme
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
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


//private val handler = Handler(Looper.getMainLooper())

//@Composable
//private fun startAnimation() {
//    val duration = 3000L
//    val infiniteTransition = rememberInfiniteTransition()
//    val animatedValue by infiniteTransition.animateFloat(
//        initialValue = -0.5f,
//        targetValue = 0.5f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(durationMillis = duration.toInt(), easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ), label = ""
//    )
//
//    handler.post(object : Runnable {
//        override fun run() {
//            val newVertices = floatArrayOf(
//                animatedValue, -0.5f, 0.0f,
//                0.5f,  animatedValue, 0.0f,
//                -0.5f,  animatedValue, 0.0f,
//                0.5f, -animatedValue, 0.0f
//            )
//            renderer.updateVertices(newVertices)
//            glSurfaceView.requestRender()
//            handler.postDelayed(this, 16) // 60 FPS
//        }
//    })
//}



@Singleton
@AndroidEntryPoint
@androidx.media3.common.util.UnstableApi
class MainActivity : ComponentActivity() {

    @MainAudioMixerPump
    @Inject
    lateinit var audioMixerPump: AudioMixerPump

    @Inject
    lateinit var utils: UtilsKT

    @Inject
    lateinit var appPath: AppPath


    override fun onPause() {
        presetsSaveFile("default", appPath.config, audioMixerPump.gen)
        //R.drawable.add
        super.onPause()
        println("...................onPause")
        //exitProcess(0)
    }


    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //renderer = MyGLRenderer()

        //Timber.plant(Timber.DebugTree())
        Timber.i("..................................onCreate.................................")

        Utils.ContextMainActivity = applicationContext


        audioMixerPump.run()

        //play()


        //GlobalScope.launch(Dispatchers.IO) {
        //player.playUri()
        //exoplayer = PlayerMP3(applicationContext)
        //}

        //Bugsnag.notify(RuntimeException("Test error"))

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



                Timber.tag("Время работы")
                        .i("!!! MainActivity запуск Navigation он начала запуска Splash: ${System.currentTimeMillis() - startTimeSplashScreenActivity} мс!!!")

                Timber.tag("Время работы")
                    .i("!!! MainActivity запуск Navigation он начала запуска App: ${System.currentTimeMillis() - startTimeAplication} мс!!!")

                Navigation()

                //OpenGLComposeView()
                //startAnimation()

//                val signalLevels = remember { // Исходные данные для графика
//                    floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 0.15f,0.18f)
//                }
//                SignalGraph(signalLevels)


            }
        }
    }
}


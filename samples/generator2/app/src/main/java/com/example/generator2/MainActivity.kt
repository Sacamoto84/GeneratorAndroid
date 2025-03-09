package com.example.generator2

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.privacysandbox.tools.core.model.Type
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.presets.presetsSaveFile
import com.example.generator2.theme.Generator2Theme
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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


class HomeScreenModel @Inject constructor() : ScreenModel {
    // ...
}

class HomeScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { HomeScreenModel() }

        val navigator = LocalNavigator.currentOrThrow
        Column {
            Text("Hello, World!", color = Color.White)

            Button(onClick = { navigator.push(HomeScreen2()) }) {

            }

        }

    }
}

class HomeScreen2 : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { HomeScreenModel() }

        val navigator = LocalNavigator.currentOrThrow

        Column {

            Text("2!", color = Color.White)

            Button(onClick = { navigator.pop() }) {

            }

        }


    }
}


@Singleton
@AndroidEntryPoint
@androidx.media3.common.util.UnstableApi
class MainActivity : ComponentActivity() {

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
        //enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        //WindowCompat.setDecorFitsSystemWindows(window, false) // Поддержка WindowInsets
        //WindowCompat.setDecorFitsSystemWindows(window, true)
        //renderer = MyGLRenderer()

//        WindowCompat.getInsetsController(window, window.decorView)
//            .isAppearanceLightStatusBars = false

        //val windowInsetsController =
        //    WindowCompat.getInsetsController(window, window.decorView)
        // Hide the system bars.
        //windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        //Timber.plant(Timber.DebugTree())

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Adjust status and navigation bar appearance
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        Timber.i("..................................onCreate.................................")

        Utils.ContextMainActivity = applicationContext

        // Запускаем корутину в потоке с высоким приоритетом
        val highPriorityThread = Thread {
            runBlocking {
                val highPriorityCoroutine = launch(Dispatchers.Default) {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                    audioMixerPump.run()
                }
                highPriorityCoroutine.join()
            }
        }
        highPriorityThread.start()

        //play()

        //GlobalScope.launch(Dispatchers.IO) {
        //player.playUri()
        //exoplayer = PlayerMP3(applicationContext)
        //}

        //Bugsnag.notify(RuntimeException("Test error"))

        //var a = 5
        //a = a/0

        Spectrogram.startFFTLoop()

        startForegroundService()

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
                    .i("!!! MainActivity запуск Navigation он начала запуска App: ${System.currentTimeMillis() - App.startTimeAplication} мс!!!")

                //   Navigation()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    Navigator(AppScreen.Home)
                }



                //OpenGLComposeView()
                //startAnimation()

//                val signalLevels = remember { // Исходные данные для графика
//                    floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 0.15f,0.18f)
//                }
//                SignalGraph(signalLevels)


            }
        }
    }

    private fun startForegroundService() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, SoundService::class.java)
                startForegroundService(intent)
            } else {
                Timber.w("Notification permission not granted: Requesting permission...")
                // Запрашиваем разрешение через launcher
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        } else {
            val intent = Intent(this, SoundService::class.java)
            startForegroundService(intent)
        }
    }

    // Создаем launcher для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение предоставлено, запускаем сервис
            val intent = Intent(this, SoundService::class.java)
            startForegroundService(intent)
        } else {
            Timber.w("Notification permission denied: Foreground service cannot start")
        }
    }

}


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
import com.example.generator2.di.Hub
import com.example.generator2.model.mmkv
import com.example.generator2.presets.presetsInit
import com.example.generator2.presets.presetsSaveFile
import com.example.generator2.theme.Generator2Theme
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.update.Update
import com.example.generator2.util.Utils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tencent.mmkv.MMKV
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import libs.KeepScreenOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

val API_key = "f6c5d62e-e201-4d03-8322-b7e738a4759f"

@Singleton
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var hub: Hub

    override fun onPause() {
        presetsSaveFile("default", AppPath().config)

        val s = mmkv.m.actualSize()
        super.onPause()
        println("...................onPause $s")
        //exitProcess(0)
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Creating an extended library configuration.
        val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build() // Initializing the AppMetrica SDK.
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(applicationContext, config) // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(application)
        YandexMetrica.reportEvent("Запуск")

        AndroidPufferDB.init(applicationContext)

        Timber.plant(Timber.DebugTree())

        Timber.i("...........................................................................")
        Timber.i("..................................onCreate.................................")
        Timber.i("...........................................................................")

        val rootDir = MMKV.initialize(this, AppPath().config)
        println("mmkv root: $rootDir")

        presetsInit()

        initialization(applicationContext, hub)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            customTypeface = resources.getFont(R.font.jetbrains)
//        }

        Utils.ContextMainActivity = applicationContext

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

                var granded by remember {
                    mutableStateOf(false)
                }

                if (!PermissionStorage.hasPermissions(this)) {

                    LaunchedEffect(key1 = true, block = {
                        while (!granded) {
                            delay(100)
                            granded = PermissionStorage.hasPermissions(applicationContext)
                        }
                    })

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (!granded) Color.Magenta else Color.Magenta),
                        Arrangement.Center
                    )
                    {
                        Text(
                            text = "Отсуствуют Файловые разрешения",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { PermissionStorage.requestPermissions(applicationContext) }) {
                            Text(
                                text = "Запрос",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                } else {

                    Update.run(applicationContext)

                    Navigation()

                }
            }
        }
    }
}


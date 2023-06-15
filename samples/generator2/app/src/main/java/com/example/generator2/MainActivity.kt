package com.example.generator2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.pufferdb.android.AndroidPufferDB
import com.example.generator2.di.Hub
import com.example.generator2.model.mmkv
import com.example.generator2.presets.presetsInit
import com.example.generator2.theme.Generator2Theme
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.update.Update
import com.example.generator2.util.Utils
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import libs.KeepScreenOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess


@Singleton
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //private val global: VMMain4 by viewModels()

    @Inject
    lateinit var hub: Hub

    override fun onPause() {
        mmkv.saveConfig()
        mmkv.saveImpulse()
        val s = mmkv.m.actualSize()
        super.onPause()
        println("...................onPause $s")
        exitProcess(0)
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(
        ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class,
        DelicateCoroutinesApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidPufferDB.init(applicationContext)

        Timber.plant(Timber.DebugTree())

//        GlobalScope.launch(Dispatchers.IO) {
//
//            val localVersion = getVersionName(applicationContext)
//            localVersion
//
//            val v = gitHubReleaseTags("dropbox", "dropbox-sdk-java")
//            v
//            val t = gitHubReleaseFiles("dropbox", "dropbox-sdk-java", "v5.0.0")
//            t
//
//        }

        Timber.i("...........................................................................")
        Timber.i("..................................onCreate.................................")
        Timber.i("...........................................................................")


        val rootDir = MMKV.initialize(this, AppPath().config)
        println("mmkv root: $rootDir")

        presetsInit()


        if (!isInitialized) {
            val t = this
            GlobalScope.launch(Dispatchers.IO) {
                Timber.i("Initialize Firebase Start")
                // Initialize Firebase Auth

                //global.hub.firebase.auth = Firebase.auth
                //global.hub.firebase.componentActivity = t

                //val storage = Firebase.storage
                Timber.i("Initialize Firebase End")
            }
        }

        initialization(applicationContext, hub)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            customTypeface = resources.getFont(R.font.jetbrains)
//        }


        //sdPath.mkdirs()


        //        //gs://test-e538d.appspot.com/
//        val storageRef = global.storage.reference //Коjрневая папка
//
//        val imagesRef: StorageReference = storageRef.child("/shared/")
//        val localFile = File.createTempFile("images", ".jpg")
//
//        imagesRef.listAll()
//            .addOnSuccessListener {
//               println( "listAll addOnSuccessListener" +it.items.joinToString(","))
//        }.addOnFailureListener{
//                println("listAll addOnFailureListener:$it")
//            }

        //readMetaBackupFromFirebase(global)

        //saveBackupToFirebase(global)

//        imagesRef.getFile(localFile)
//            .addOnSuccessListener {
//                // Local temp file has been created
//                Toast.makeText( applicationContext, "imagesRef: success", Toast.LENGTH_LONG ).show()
//            }.addOnFailureListener {
//                // Handle any errors
//                Toast.makeText( applicationContext, "imagesRef: Error", Toast.LENGTH_LONG ).show()
//            }

        Utils.ContextMainActivity = applicationContext

        //Hawk.init(this).setLogInterceptor { message -> Log.i("HAWK:", message) }.build()

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
            Generator2Theme() {

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


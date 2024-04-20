package com.example.generator2


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.noSQL.KEY_NOSQL_CONFIG2
import com.example.generator2.features.playlist.Playlist
import com.example.generator2.features.scope.Scope
import com.example.generator2.features.update.Update
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.findActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.skeptick.libres.LibresSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var gen: Generator

    @Inject
    lateinit var scope: Scope

    @UnstableApi
    @Inject
    lateinit var audioMixerPump: AudioMixerPump

    @Inject
    lateinit var utils: UtilsKT

    @Inject
    lateinit var appPath: AppPath

    @Inject
    lateinit var global: Global

    @Inject
    lateinit var update: Update

    @Inject
    lateinit var playlist: Playlist

    @Inject
    lateinit var initialization : Initialization

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //Sherlock.init(this) //Initializing Sherlock
        //Sherlock.getInstance().getAllCrashes()

//        val item1 =
//            PlaylistItem(name = "Один", path = AppPath().music + "/one.mp3", isPresent = true)
//        val item2 =
//            PlaylistItem(name = "Два", path = AppPath().music + "/two.mp3", isPresent = true)
//        val item3 =
//            PlaylistItem(name = "три", path = AppPath().music + "/one3.mp3", isPresent = true)
//        val item4 =
//            PlaylistItem(name = "четыре", path = AppPath().music + "/two4.mp3", isPresent = true)
//        val pl1 = listOf(item1, item2).toMutableList()
//        val pl2 = listOf(item3, item4).toMutableList()
//
//        val list1 = Playlist(playlistName = "1111", data = pl1)
//        val list2 = Playlist(playlistName = "2222", data = pl2)
//
//        playlist.addAll(mutableListOf(list1, list2))


//        val item1 = PlaylistItemJson( path = AppPath().music + "/one.mp3", additionalData = mapOf("q" to 5, "qqq" to 10))
//        val item2 = PlaylistItemJson( path = AppPath().music + "/2.mp3")
//        val item3 = PlaylistItemJson( path = AppPath().music + "/3.mp3")
//        val item4 = PlaylistItemJson( path = AppPath().music + "/4.mp3")
//        val list1 = PlaylistJson(playlistName = "1111", data = listOf(item1, item2).toMutableList())
//        val list2 = PlaylistJson(playlistName = "2222", data =  listOf(item3, item4).toMutableList())
//        playlistJson.addAll(listOf(list1, list2))


        //PlaylistJSON().write(playlistJson)

        //playlistJson.addAll(PlaylistSQL.read())
        //playlistJson

        playlist

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return
//        }
//        LeakCanary.install(this)
//        // Normal app init code...

        val window = this.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this@SplashScreenActivity, PermissionScreenActivity::class.java)
            startActivity(intent)
            finish()
        } else {

            setContentView(R.layout.activity_splash_screen)



            if (initialization.isInitialized) {
                //GlobalScope.launch(Dispatchers.Main) {
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
                return
                //}
            }

            //noSQLConfig2.write(KEY_NOSQL_CONFIG2.LANGUAGE.value, "ru")

            //Читаем язык
            LibresSettings.languageCode =
                global.noSQLConfig2.read(KEY_NOSQL_CONFIG2.LANGUAGE.value, "ru")

            val myTextView: TextView = findViewById(R.id.myTextView)
            myTextView.text = MainRes.string.splashLoading


//            GlobalScope.launch(Dispatchers.IO) {
//                println("Запуск Yandex Metrika")
//                val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build()
//                YandexMetrica.activate(this@SplashScreenActivity, config)
//                YandexMetrica.enableActivityAutoTracking(application)
//                YandexMetrica.reportEvent("Запуск")
//            }


            GlobalScope.launch(Dispatchers.IO) {

                initialization.run()
                audioOut
                audioMixerPump
                scope
                update.run()

                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
                return@launch
            }

        }
    }
}
package com.example.generator2


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.initialization.Initialization
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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

var startTimeSplashScreenActivity = System.currentTimeMillis()

@Singleton
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    @UnstableApi
    @Inject
    lateinit var audioMixerPump: AudioMixerPump

    @Inject
    lateinit var appPath: AppPath

    @Inject
    lateinit var global: Global

    //@Inject
    //lateinit var update: Update

    @Inject
    lateinit var initialization : Initialization

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Timber.tag("Время работы").i("!!! SplashActivity начало !!!")
        startTimeSplashScreenActivity = System.currentTimeMillis()

        val window = this.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this@SplashScreenActivity, PermissionScreenActivity::class.java)
            startActivity(intent)
            finish()
        } else {

            setContentView(R.layout.activity_splash_screen)

            /* При повторном запуске */
            if (initialization.isInitialized) {
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
                return
            }

            //noSQLConfig2.write(KEY_NOSQL_CONFIG2.LANGUAGE.value, "ru")

            //Читаем язык
            LibresSettings.languageCode = global.noSQLConfig2.read(KEY_NOSQL_CONFIG2.LANGUAGE.value, "ru")

            val myTextView: TextView = findViewById(R.id.myTextView)
            myTextView.text = MainRes.string.splashLoading

            GlobalScope.launch(Dispatchers.IO) {

                initialization.run()
                audioOut
                audioMixerPump
                //update.run()

                val endTime = System.currentTimeMillis()
                val elapsedTime = endTime - startTimeSplashScreenActivity
                println("Время выполнения кода: $elapsedTime мс")
                Timber.tag("Время работы").i("!!! SplashActivity завершена: $elapsedTime мс!!!")

                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
                return@launch
            }

        }
    }
}
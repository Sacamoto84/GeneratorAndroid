package com.example.generator2


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import cafe.adriel.pufferdb.android.AndroidPufferDB
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.generator.Generator
import com.example.generator2.model.itemList
import com.example.generator2.presets.presetsInit
import com.example.generator2.scope.Scope
import com.example.generator2.update.Update
import com.example.generator2.update.kDownloader
import com.example.generator2.util.Utils
import com.example.generator2.util.UtilsKT
import com.example.generator2.util.findActivity
import com.kdownloader.KDownloader
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.AndroidEntryPoint
import flipagram.assetcopylib.AssetCopier
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
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

    @Inject
    lateinit var audioMixerPump: AudioMixerPump

    @Inject
    lateinit var utils: UtilsKT

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return
//        }
        //LeakCanary.install(this)
//        // Normal app init code...

        val window = this.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Timber.plant(Timber.DebugTree())

        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this@SplashScreenActivity, PermissionScreenActivity::class.java)
            startActivity(intent)
            finish()
        } else {

            setContentView(R.layout.activity_splash_screen)

            GlobalScope.launch(Dispatchers.IO) {
                println("Запуск Yandex Metrika")
                val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build()
                YandexMetrica.activate(this@SplashScreenActivity, config)
                YandexMetrica.enableActivityAutoTracking(application)
                YandexMetrica.reportEvent("Запуск")
            }


            GlobalScope.launch(Dispatchers.IO) {

                println("Типа инициализация Splash")
                val path = AppPath()
                path.mkDir()

                val patchCarrier = path.carrier
                val patchMod = path.mod

                Utils.patchDocument = path.main
                Utils.patchCarrier = "$patchCarrier/"
                Utils.patchMod = "$patchMod/"

                try {
                    AssetCopier(this@SplashScreenActivity).copy("Carrier", File("$patchCarrier/"))
                    AssetCopier(this@SplashScreenActivity).copy("Mod", File(patchMod))
                } catch (e: IOException) {
                    Timber.e(e.printStackTrace().toString())
                }

                println("arrFilesCarrier start")
                val arrFilesCarrier: Array<String> = Utils.listFileInCarrier() //Заполняем список
                for (i in arrFilesCarrier.indices) {
                    gen.itemlistCarrier.add(itemList(patchCarrier, arrFilesCarrier[i], 0))
                }

                val arrFilesMod: Array<String> =
                    Utils.listFileInMod() //Получение списка файлов в папке Mod
                for (i in arrFilesMod.indices) {
                    gen.itemlistAM.add(itemList(patchMod, arrFilesMod[i], 1))
                    gen.itemlistFM.add(itemList(patchMod, arrFilesMod[i], 0))
                }
                println("Запуск MainActivity")

                gen
                kDownloader = KDownloader.create(applicationContext)

                AndroidPufferDB.init(applicationContext)
                presetsInit()

                initialization(applicationContext, gen, utils)
                audioOut
                audioMixerPump
                scope

                Update.run(applicationContext)

                GlobalScope.launch(Dispatchers.Main) {
                    val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                }

            }

        }
    }
}
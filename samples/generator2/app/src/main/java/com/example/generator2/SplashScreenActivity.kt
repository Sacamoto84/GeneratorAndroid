package com.example.generator2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.example.generator2.generator.Generator
import com.example.generator2.model.itemList
import com.example.generator2.util.Utils
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

    private lateinit var videoView: VideoView

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this@SplashScreenActivity, PermissionScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {


            setContentView(R.layout.activity_splash_screen)

            videoView = findViewById(R.id.videoView)
            val videoPath =
                "android.resource://" + packageName + "/" + R.raw.q1 // путь к вашему видео
            val uri = Uri.parse(videoPath)
            videoView.setVideoURI(uri)

            videoView.setOnCompletionListener {
//            // После окончания воспроизведения видео, перейдите на следующий экран или активность
//            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
//            startActivity(intent)
//            finish()
                videoView.start()
            }

            GlobalScope.launch(Dispatchers.Main) {
                videoView.start()
            }

            val contex = this

            GlobalScope.launch(Dispatchers.Main) {

                println("Типа инициализация Splash")
                val path = AppPath()
                path.mkDir()

                val patchCarrier = path.carrier
                val patchMod = path.mod

                Utils.patchDocument = path.main
                Utils.patchCarrier = "$patchCarrier/"
                Utils.patchMod = "$patchMod/"

                try {
                    AssetCopier(contex).copy("Carrier", File("$patchCarrier/"))
                    AssetCopier(contex).copy("Mod", File(patchMod))
                } catch (e: IOException) {
                    Timber.e(e.printStackTrace().toString())
                }

                Timber.i("arrFilesCarrier start")
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

                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
    }
}
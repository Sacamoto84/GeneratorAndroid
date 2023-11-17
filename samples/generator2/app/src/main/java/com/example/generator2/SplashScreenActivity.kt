package com.example.generator2



import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.TextureView
import android.widget.ImageView
import android.widget.VideoView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.caverock.androidsvg.SVGParseException
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


        lateinit var textureView: TextureView
        lateinit var mediaPlayer: MediaPlayer


        super.onCreate(savedInstanceState)

        //val window = this.findActivity()?.window
        //window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this@SplashScreenActivity, PermissionScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {

            setContentView(R.layout.activity_splash_screen)

            //val videoPath = "android.resource://" + packageName + "/" + R.raw.sticker // путь к вашему видео
            //val uri = Uri.parse(videoPath)

            //videoView.setVideoURI(uri)


            // Загрузка SVG из файла
//            try {
//                val inputStream = assets.open("806.svg")
//                val svg = SVG.getFromInputStream(inputStream)
//                val svgImageView = findViewById<SVGImageView>(R.id.svgImageView)
//                svgImageView.setSVG(svg)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } catch (e: SVGParseException) {
//                e.printStackTrace()
//            }
//
            val imageView = findViewById<ImageView>(R.id.imageView)

            val videoPath = "android.resource://" + packageName + "/" + R.raw.q2 // путь к вашему видео

            Glide.with(this)
                .asGif()
                .load(videoPath)
                .into(imageView)

//            mediaPlayer.setOnPreparedListener {
//                // Когда mediaPlayer готов к воспроизведению
//                mediaPlayer.start()
//            }


//            videoView.setOnPreparedListener { mediaPlayer ->
//                val videoWidth = mediaPlayer.videoWidth
//                val videoHeight = mediaPlayer.videoHeight
//
//                // Здесь вы можете использовать полученные размеры
//                val params = videoView.layoutParams
//                params.width = videoWidth / 4 // Устанавливаем ширину в 50% от исходной
//                params.height = (videoHeight.toFloat() / videoWidth.toFloat() * (videoWidth / 2)).toInt() // Подстраиваем высоту для сохранения соотношения сторон
//                videoView.layoutParams = params
//
//                val matrix = Matrix()
//                val scaleX = videoView.width.toFloat() / videoWidth.toFloat()
//                val scaleY = videoView.height.toFloat() / videoHeight.toFloat()
//                matrix.setScale(scaleX, scaleY)
//
//                videoView.transformMatrixToLocal(matrix)
//
//                videoView.start()
//
//            }


            //videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN)

//            videoView.setOnCompletionListener {
////            // После окончания воспроизведения видео, перейдите на следующий экран или активность
////            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
////            startActivity(intent)
////            finish()
//                videoView.start()
//            }

//            GlobalScope.launch(Dispatchers.Main) {
//                videoView.start()
//            }

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
                //startActivity(intent)
                //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                //finish()
            }

        }
    }
}
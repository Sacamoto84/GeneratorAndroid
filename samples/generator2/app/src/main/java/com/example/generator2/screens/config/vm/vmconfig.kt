package com.example.generator2.screens.config.vm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.startActivity
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.generator2.R
import com.example.generator2.common.snackbar.SnackBar
import com.example.generator2.features.update.UPDATESTATE
import com.example.generator2.features.update.Update
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.settings.Settings
import com.example.generator2.model.LiveConstrain
import com.yagmurerdogan.toasticlib.Toastic
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.skeptick.libres.LibresSettings
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
class VMConfig @Inject constructor(
    @ApplicationContext
    private val context: Context,
    val gen: Generator,
    val update: Update,
    val settings: Settings
) : ScreenModel {

    val recompose = mutableStateOf(0)

    //var LVolume by  mutableStateOf(0.55F)
    //var RVolume by  mutableStateOf(0.65F)

    fun openTelegram() {
        //val telegram = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/GGenerator2"))
        //telegram.setPackage("org.telegram.messenger")
        //startActivity( context, telegram, null)

        try {

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=GGenerator2"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pm: PackageManager = context.packageManager
            if (intent.resolveActivity(pm) != null) {

                startActivity(context, intent, null)
            } else {
                Toast.makeText(context, "Error message", Toast.LENGTH_LONG).show()
            }

        } catch (ignored: java.lang.Exception) {
            Toast.makeText(context, "Error ignored $ignored", Toast.LENGTH_LONG).show()
            Timber.e(ignored)
        }

    }

    /** Сохранить максимальную громкость обоих каналов */
    fun saveVolume() = screenModelScope.launch {
        settings.update {
            it.copy(
                maxVolume0 = gen.liveData.maxVolume0.value,
                maxVolume1 = gen.liveData.maxVolume1.value
            )
        }
    }

    /** Сохранить чувствительность слайдеров */
    fun saveConstrain() = screenModelScope.launch {
        settings.update {
            it.copy(
                sensitivitySliderCr = LiveConstrain.sensetingSliderCr.floatValue,
                sensitivitySliderFmDev = LiveConstrain.sensetingSliderFmDev.floatValue,
                sensitivitySliderAmFm = LiveConstrain.sensetingSliderAmFm.floatValue
            )
        }
    }

    fun toastSaveVolume() {

        SnackBar.success("Volume Saved")

//        Toastic.toastic(
//            context = context,
//            message = "Volume Saved",
//            duration = Toastic.LENGTH_SHORT,
//            type = Toastic.SUCCESS,
//            //isIconAnimated = true,
//            customIcon = R.drawable.info3,
//            font = R.font.jetbrains,
//            customBackground = R.drawable.toast_bg,
//            textColor = Color.WHITE,
//            //customIconAnimation = androidx.appcompat.R.anim.abc_slide_out_bottom
//        ).show()

    }


    fun toastText(str: String) {
        Toastic.toastic(
            context = context,
            message = str,
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.SUCCESS,
            //isIconAnimated = true,
            //customIcon = R.drawable.info3,
            font = R.font.jetbrains,
            customBackground = R.drawable.toast_bg,
            textColor = Color.WHITE,
            //customIconAnimation = androidx.appcompat.R.anim.abc_slide_out_bottom
        ).show()
    }

///////////////////////////
    /**
     * ### Сохранения языка
     * @param str Сохраняемый язык "ru" "Русский" "en" "English"
     */
    fun saveLanguage(str: String = "ru") {

        val out = when (str) {
            "Русский", "ru" -> "ru"
            "English", "en" -> "en"
            else -> "ru"
        }

        //Читаем язык
        LibresSettings.languageCode = out

        //Сохранение в настройки
        screenModelScope.launch { settings.update { it.copy(language = out) } }

    }
///////////////////////////
    /**
     * ### Узнать текущия язык
     * @return "en" "ru"
     */
    fun readLanguage() = LibresSettings.languageCode
///////////////////////////
    /**
     * ### Рекомпозиция экрана
     */
    fun recompose() {
        recompose.value++
    }
///////////////////////////

    fun update() {
        update.state.value = UPDATESTATE.DOWNLOADING
    }

}
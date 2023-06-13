package com.example.generator2.screens.config.vm

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.generator2.R
import com.example.generator2.backup.Backup
import com.example.generator2.element.Firebas
import com.example.generator2.model.mmkv
import com.yagmurerdogan.toasticlib.Toastic
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


//Сообщения по поводу метаданных бекапа
val strMetadataError      = MutableStateFlow("")     //Текст ошибок для мета данных
val strMetadata           = MutableStateFlow("")     //Текст сообщения для мета данных
val progressMetadata      = MutableStateFlow(false)  //Текст сообщения для мета данных

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VMConfig @Inject constructor(
    @ApplicationContext
    private val context: Context,
    var backup: Backup,
    var firebase : Firebas
) : ViewModel(){


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

                startActivity( context, intent, null)
            }
            else {
                Toast.makeText(context, "Error message", Toast.LENGTH_LONG).show()
            }

        } catch (ignored: java.lang.Exception) {
            Toast.makeText(context, "Error ignored $ignored", Toast.LENGTH_LONG).show()
            println(ignored)
        }

    }

    fun saveVolume()    = mmkv.saveVolume() //backup.json.saveJsonVolume()
    fun saveConstrain() = mmkv.saveConstrain() //backup.json.saveJsonConstrain()

    fun toastSaveVolume()
    {
        Toastic.toastic(
            context = context,
            message = "Volume Saved",
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.SUCCESS,
            //isIconAnimated = true,
            customIcon = R.drawable.info3,
            font = R.font.jetbrains,
            customBackground = R.drawable.toast_bg,
            textColor = Color.WHITE,
            //customIconAnimation = androidx.appcompat.R.anim.abc_slide_out_bottom
        ).show()
    }



    fun toastText(str : String)
    {
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

}
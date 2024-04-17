package com.example.generator2.features.update.mono

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File


fun installAPK(context: Context, f: File) {

    val path = f.absolutePath

    val file = File(path)
    if (file.exists()) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            uriFromFile(context, File(path)),
            "application/vnd.android.package-archive"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Timber.tag("Install Apk").e("Ошибка открытия файла!")
        }
    } else {
        Toast.makeText(context, "installing", Toast.LENGTH_LONG).show()
    }
}

private fun uriFromFile(context: Context?, file: File?): Uri? {
    return FileProvider.getUriForFile(context!!, context.packageName + ".provider",  file!! )
}

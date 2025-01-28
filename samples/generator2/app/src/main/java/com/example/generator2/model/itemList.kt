package com.example.generator2.model

import android.graphics.Bitmap
import android.graphics.Color
import com.example.generator2.App
import com.example.generator2.features.initialization.utils.createBitmapCarrier
import com.example.generator2.features.initialization.utils.createBitmapModulation
import com.example.generator2.features.initialization.utils.readBytesFromAssets
import timber.log.Timber

class itemList(
    private val path: String, // Путь к папке
    private val filename: String, // файл.dat
    mod: Int
) {

    var name = filename.replace(".dat", "") // название без окончания

    var bitmap: Bitmap? = null //Картинка несущей

    var buf = ByteArray(2048)

    //Конструктор
    init {

        name = filename.replace(".dat", "")

        try {

            buf = readBytesFromAssets(App.application, path, filename, 2048)!!

            bitmap =
                if (mod == 0)
                    createBitmapCarrier(buf)
                else
                    createBitmapModulation(buf)

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            bitmap = Bitmap.createBitmap(1024 / 2, 512 / 2, Bitmap.Config.RGB_565)
            bitmap!!.eraseColor(Color.RED)
        }

    }

}

package com.example.generator2.model

import android.graphics.Bitmap
import com.example.generator2.util.Utils

class itemList(// Путь к файлу
    private val path: String, // Название файла
    private val filename: String,
    mod: Int
) {

    var name = "xxx" // название без окончания
    var bitmap : Bitmap? = null //Картинка несущей
    //var buf : ByteArray? = null

    //= Utils.readFileMod2048byte(path) //Здесь должны прочитать файл и записать в массив;

    //Конструктор
    init {
        name = filename.replace(".dat", "")
        bitmap =
            if (mod == 0)
                Utils.CreateBitmapCarrier2("$path/$filename")
            else
                Utils.CreateBitmapModulation("$path/$filename" )

        //buf = Utils.readFileMod2048byte(path + filename) //Здесь должны прочитать файл и записать в массив;
    }



}
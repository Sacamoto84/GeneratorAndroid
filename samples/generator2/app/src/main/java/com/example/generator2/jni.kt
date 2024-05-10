package com.example.generator2

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

class FastBitmap {

    init
    {
        System.loadLibrary("generator2")
    }

    var bitmap: Bitmap = createBitmap(1, 1,  config = Bitmap.Config.RGB_565)

    var WIDTH = 1
    var HEIGHT = 1


    fun initEngine()
    {
        bitmap = createBitmap(WIDTH, HEIGHT,  config = Bitmap.Config.RGB_565)
        createBuffer(WIDTH, HEIGHT)
    }


    private external fun createBuffer(w : Int, h : Int)

    external fun setPixel(x : Int, y : Int, color : Int)

    //Прочитать TFT буффер и записать в bitmap
    external fun processBitmap(bitmap: Bitmap)


}
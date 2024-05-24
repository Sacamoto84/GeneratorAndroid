package com.example.generator2.features.scope

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

data class Frame(val bitmap: Bitmap, var frame: Long)

class BitmapPool(maxSize: Int) {

    val pool = Array(maxSize){
        Frame(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), it.toLong())
    }

    val update = MutableStateFlow(0L)

    //Номер кадра
    private var frame: Long = maxSize.toLong() - 1

    init {

        for(i in pool.indices) {
            pool[i] = Frame(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), i.toLong())
        }



    }

    //@Synchronized
    fun getBitmap(width: Int, height: Int, config: Bitmap.Config): Frame {

        //Минимальный кадр в пуле
        val indexMin = findMinFrameIndex(pool)

        //Если найден
        if (indexMin != -1) {

            //Проверка битмапа что он соответствует размеру
            if (pool[indexMin].bitmap.width == width && pool[indexMin].bitmap.height == height && pool[indexMin].bitmap.config == config) {
                frame++
                pool[indexMin].frame = frame
                pool[indexMin].bitmap.eraseColor(Color.Black.toArgb())
                Timber.d("Битмап найден i:${indexMin} frame:${frame}")
                return pool[indexMin]
            }
            else {
                frame++
                pool[indexMin] = Frame(Bitmap.createBitmap(width, height, config), frame)

                Timber.d("Битмап не верный размер, создаем новый i:${indexMin} frame:${frame}")

                return pool[indexMin]
            }
        }

        Timber.d("Залупа конская, создаем новый i:${indexMin} frame:${frame}")

        //Все занято, нет свободных ячеек
        return Frame(Bitmap.createBitmap(width, height, config), -1)
    }


    private fun findMinFrameIndex(pool: Array<Frame>): Int {
        if (pool.isEmpty()) return -1

        var minIndex = 0
        var minFrameValue = pool[0].frame

        for (i in 1 until pool.size) {
            val frameValue = pool[i].frame
            if (frameValue < minFrameValue) {
                minFrameValue = frameValue
                minIndex = i
            }
        }

        return minIndex
    }

    //Поиск индекса елелемента в пуле по номеру кадра, если в пуле нет данного кадра, то индекс возвращает -1
    fun findFrameIndex(frame: Long): Int {
        var index = -1
        for (i in pool.indices) {
            if (pool[i].frame == frame) {
                index = i
                break
            }
        }
        return index
    }


}
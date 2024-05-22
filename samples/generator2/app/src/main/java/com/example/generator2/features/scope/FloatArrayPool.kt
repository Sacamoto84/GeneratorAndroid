package com.example.generator2.features.scope

import timber.log.Timber


data class FloatArrayFrame(var array: FloatArray, var frame: Long)

class FloatArrayPool(maxSize: Int) {

    val pool = Array(maxSize) {
        FloatArrayFrame(FloatArray(1), it.toLong())
    }

    //Номер кадра
    private var frame: Long = maxSize.toLong() - 1

    init {
        for (i in pool.indices) {
            pool[i] = FloatArrayFrame(FloatArray(1), i.toLong())
        }
    }

    @Synchronized
    fun getFloatArrayFrame(len: Int): FloatArrayFrame {

        //Минимальный кадр в пуле
        val indexMin = findMinFrameIndex(pool)

        //Если найден
        if (indexMin != -1) {

            //Проверка битмапа что он соответствует размеру
            if (pool[indexMin].array.size == len) {
                frame++
                pool[indexMin].frame = frame
                Timber.d("FloatArray найден i:${indexMin} frame:${frame}")
                return pool[indexMin]
            } else {
                frame++
                pool[indexMin] = FloatArrayFrame(FloatArray(len), frame)
                Timber.d("FloatArray не верный размер, создаем новый i:${indexMin} frame:${frame}")
                return pool[indexMin]
            }
        }

        Timber.d("Залупа конская, создаем новый FloatArray i:${indexMin} frame:${frame}")

        //Все занято, нет свободных ячеек
        return FloatArrayFrame(FloatArray(len), -1)

    }

    //Поиск индекса елемента с минимальным номером кадра
    private fun findMinFrameIndex(pool: Array<FloatArrayFrame>): Int {
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
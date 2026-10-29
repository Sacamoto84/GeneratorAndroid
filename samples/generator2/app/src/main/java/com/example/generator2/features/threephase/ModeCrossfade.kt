package com.example.generator2.features.threephase

import kotlin.math.min

/** Переход между обычными каналами и 3-фазой, с. */
const val MODE_CROSSFADE_SEC = 0.02f

/**
 * Линейный кроссфейд старого сигнала в новый на первых [n] сэмплах:
 * `dst = old·(1 − t) + dst·t`, `t = (i + 1) / n`. Дальше [dst] не трогается.
 */
fun crossfadeFrom(oldL: FloatArray, oldR: FloatArray, dstL: FloatArray, dstR: FloatArray, n: Int) {
    val m = min(n, dstL.size)
    for (i in 0 until m) {
        val t = (i + 1).toFloat() / n
        dstL[i] = oldL[i] * (1 - t) + dstL[i] * t
        dstR[i] = oldR[i] * (1 - t) + dstR[i] * t
    }
}

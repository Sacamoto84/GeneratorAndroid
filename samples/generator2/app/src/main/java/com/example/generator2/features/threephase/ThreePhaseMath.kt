package com.example.generator2.features.threephase

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Математика трёхфазного режима, порт restim один в один
 * (`stim_math/threephase.py`, `stim_math/transforms.py`).
 *
 * Электроды: N — верх круга (Neutral), L и R — низ. Аудиоканалы:
 * `L = V_N − V_L`, `R = V_N − V_R`. Всё здесь считается вне аудиопотока,
 * кроме [sample] — это эталон посэмплового нативного рендера для тестов.
 */
object ThreePhaseMath {

    private val SQRT3 = sqrt(3.0)

    //potential_to_channel_matrix
    private val P = arrayOf(
        doubleArrayOf(1.0, -1.0, 0.0),
        doubleArrayOf(1.0, 0.0, -1.0),
        doubleArrayOf(1.0, 1.0, 1.0),
    )

    //ab_transform
    private val AB = arrayOf(
        doubleArrayOf(1.0, 0.0, 1.0),
        doubleArrayOf(-0.5, SQRT3 / 2, 1.0),
        doubleArrayOf(-0.5, -SQRT3 / 2, 1.0),
    )

    private val P_INV = inv3(P)
    private val AB_INV = inv3(AB)

    /**
     * Калибровка A/B/C (дБ, «Modern» в restim) → классическая пара
     * `[neutralDb, rightDb]` (`intensity_ratio_to_ud_lr`).
     */
    fun calibrationToUdLr(aDb: Double, bDb: Double, cDb: Double): DoubleArray {
        val calib = calibrationFromIntensities(
            10.0.pow(aDb / 10), 10.0.pow(bDb / 10), 10.0.pow(cDb / 10)
        )
        return calibrationToUdLr(calib)
    }

    /**
     * Итоговая матрица 2×2 `[K00, K01, K10, K11]`: из сигналов `(a, b)` после
     * матрицы squeeze сразу в `(L, R)` с аппаратной калибровкой и масштабом,
     * при котором |L|, |R| ≤ 1.
     */
    fun channelMatrix(aDb: Float, bDb: Float, cDb: Float): FloatArray {
        val (neutral, right) = calibrationToUdLr(aDb.toDouble(), bDb.toDouble(), cDb.toDouble())

        val transform = transformInAb(neutral, right)
        val corrective = mul3(mul3(mul3(mul3(P, AB), transform), AB_INV), P_INV)
        val scale = scalingConstant(transform)

        //T = (P @ ab)[:2, :2] / sqrt(3)
        val pab = mul3(P, AB)

        val k = FloatArray(4)
        for (row in 0..1) for (col in 0..1) {
            var sum = 0.0
            for (i in 0..1) sum += corrective[row][i] * scale * pab[i][col] / SQRT3
            k[row * 2 + col] = sum.toFloat()
        }
        return k
    }

    /**
     * Приглушение центра (`ThreePhaseCenterCalibration` для ratio ≤ 1):
     * в центре `1 − c`, на краю 1, между ними линейно. `c = reductionPct / 100`.
     */
    fun centerGain(r: Float, reductionPct: Float): Float {
        val c = reductionPct / 100f
        return 1f - c * (1f - r.coerceIn(0f, 1f))
    }

    /**
     * Один сэмпл без громкости и центра: точка (alpha, beta), фаза несущей
     * [theta], матрица [k] из [channelMatrix]. `out[0] = L`, `out[1] = R`.
     */
    fun sample(alpha: Float, beta: Float, theta: Double, k: FloatArray, out: FloatArray) {
        var a = alpha.toDouble()
        var b = beta.toDouble()
        var r = sqrt(a * a + b * b)
        if (r > 1) {
            a /= r
            b /= r
            r = 1.0
        }

        val t11 = (2 - r + a) / 2
        val t12 = -b / 2 //знак как в restim, вместе с их TODO
        val t22 = (2 - r - a) / 2

        val x = cos(theta)
        val y = sin(theta)
        val sa = t11 * x + t12 * y
        val sb = t12 * x + t22 * y

        out[0] = (k[0] * sa + k[1] * sb).toFloat()
        out[1] = (k[2] * sa + k[3] * sb).toFloat()
    }

    //─── calibration_matrix_from_electrode_intensities ───────────────────────

    private fun calibrationFromIntensities(a0: Double, b0: Double, c0: Double): Array<DoubleArray> {
        val maximum = maxOf(a0, b0, c0)
        var a = a0 / maximum
        var b = b0 / maximum
        var c = c0 / maximum

        //Должно быть min + mid >= max
        when {
            a >= b && a >= c -> if (b + c < a) {
                val offset = (a - (b + c)) / 2
                b += offset; c += offset
            }

            b >= c -> if (a + c < b) {
                val offset = (b - (a + c)) / 2
                a += offset; c += offset
            }

            else -> if (a + b < c) {
                val offset = (c - (a + b)) / 2
                a += offset; b += offset
            }
        }

        val ab = arrayOf(
            doubleArrayOf(1.0, 0.0),
            doubleArrayOf(-0.5, -SQRT3 / 2),
            doubleArrayOf(-0.5, SQRT3 / 2),
        )

        //split_point(-a, b, c): m вещественное, поэтому m_normalized = ±1
        val m = -a
        val mNormalized = if (abs(m) > .001) m / abs(m) else 1.0
        val cc = max(0.0001, abs(m))
        val rational = (b * b - c * c + cc * cc) / (2 * cc)
        val imaginary = sqrt(max(b * b - rational * rational, 0.0))
        val pRe = rational * mNormalized
        val pIm = imaginary * mNormalized
        val qRe = m - pRe
        val qIm = -pIm

        val ab2 = arrayOf(
            doubleArrayOf(a, 0.0),
            doubleArrayOf(pRe, pIm),
            doubleArrayOf(qRe, qIm),
        )

        var calib = leastSquares(ab, ab2)

        //Поворот, чтобы calib[0][1] == calib[1][0]
        val q = atan2(calib[0][1] - calib[1][0], calib[0][0] + calib[1][1])
        val rot = arrayOf(
            doubleArrayOf(cos(q), -sin(q)),
            doubleArrayOf(sin(q), cos(q)),
        )
        calib = mul2(calib, rot)

        //Наибольшее собственное значение (вещественная часть) → 1
        val tr = calib[0][0] + calib[1][1]
        val det = calib[0][0] * calib[1][1] - calib[0][1] * calib[1][0]
        val largest = tr / 2 + sqrt(max(tr * tr / 4 - det, 0.0))
        return Array(2) { row -> DoubleArray(2) { col -> calib[row][col] / largest } }
    }

    /** Решение A·X = B (A — 3×2) методом наименьших квадратов: (AᵀA)⁻¹AᵀB. */
    private fun leastSquares(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val ata = Array(2) { i -> DoubleArray(2) { j -> (0..2).sumOf { a[it][i] * a[it][j] } } }
        val atb = Array(2) { i -> DoubleArray(2) { j -> (0..2).sumOf { a[it][i] * b[it][j] } } }
        val det = ata[0][0] * ata[1][1] - ata[0][1] * ata[1][0]
        val inv = arrayOf(
            doubleArrayOf(ata[1][1] / det, -ata[0][1] / det),
            doubleArrayOf(-ata[1][0] / det, ata[0][0] / det),
        )
        return mul2(inv, atb)
    }

    //─── calibration_matrix_to_ud_lr ─────────────────────────────────────────

    private fun calibrationToUdLr(calib: Array<DoubleArray>): DoubleArray {
        //inverse_scale_in_arbitrary_direction
        val s = calib[0][0] + calib[1][1] - 2
        var a = 0.0
        var b = 0.0
        var scale = 1.0
        if (s != 0.0) {
            scale = s + 1
            a = abs((calib[0][0] - 1) / s).pow(.5)
            b = abs((calib[1][1] - 1) / s).pow(.5)
            if (calib[0][1] <= 0) b = -b
        }

        val ratio = 1 / max(scale, .001)
        val theta = -atan2(a, -b) * 2
        val norm = log10(ratio) * 10
        return doubleArrayOf(cos(theta) * norm, -sin(theta) * norm)
    }

    //─── ThreePhaseHardwareCalibration ───────────────────────────────────────

    /** generate_transform_in_ab: растяжение в произвольном направлении, 3×3. */
    private fun transformInAb(upDown: Double, leftRight: Double): Array<DoubleArray> {
        if (upDown == 0.0 && leftRight == 0.0) return identity3()
        val theta = atan2(leftRight, upDown) / 2
        val norm = sqrt(upDown * upDown + leftRight * leftRight)
        val ratio = 10.0.pow(norm / 10)

        //scale_in_arbitrary_direction(sin(-theta), cos(theta), 1/ratio)
        val a = sin(-theta)
        val b = cos(theta)
        val s = 1 / ratio - 1
        return arrayOf(
            doubleArrayOf(1 + s * a * a, s * a * b, 0.0),
            doubleArrayOf(s * a * b, 1 + s * b * b, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0),
        )
    }

    /** scaling_contant: масштаб, при котором |L|, |R| ≤ 1. */
    private fun scalingConstant(transform: Array<DoubleArray>): Double {
        val qq = mul3(mul3(P, AB), transform)
        val k1 = sqrt(qq[0][0] * qq[0][0] + qq[0][1] * qq[0][1])
        val k2 = sqrt(qq[1][0] * qq[1][0] + qq[1][1] * qq[1][1])
        return 1 / max(k1, k2) * SQRT3
    }

    //─── матрицы ─────────────────────────────────────────────────────────────

    private fun identity3() = Array(3) { i -> DoubleArray(3) { j -> if (i == j) 1.0 else 0.0 } }

    private fun mul3(x: Array<DoubleArray>, y: Array<DoubleArray>) =
        Array(3) { i -> DoubleArray(3) { j -> (0..2).sumOf { x[i][it] * y[it][j] } } }

    private fun mul2(x: Array<DoubleArray>, y: Array<DoubleArray>) =
        Array(2) { i -> DoubleArray(2) { j -> (0..1).sumOf { x[i][it] * y[it][j] } } }

    private fun inv3(m: Array<DoubleArray>): Array<DoubleArray> {
        val det = m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
                m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
        fun cof(r0: Int, c0: Int, r1: Int, c1: Int) = m[r0][c0] * m[r1][c1] - m[r0][c1] * m[r1][c0]
        return arrayOf(
            doubleArrayOf(cof(1, 1, 2, 2) / det, -cof(0, 1, 2, 2) / det, cof(0, 1, 1, 2) / det),
            doubleArrayOf(-cof(1, 0, 2, 2) / det, cof(0, 0, 2, 2) / det, -cof(0, 0, 1, 2) / det),
            doubleArrayOf(cof(1, 0, 2, 1) / det, -cof(0, 0, 2, 1) / det, cof(0, 0, 1, 1) / det),
        )
    }
}

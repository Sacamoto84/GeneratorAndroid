package com.example.generator2.features.threephase

import com.example.generator2.features.threephase.patterns.ThreePhasePattern
import com.example.generator2.features.threephase.patterns.ThreePhasePatternSet
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min

/**
 * Движок положения точки трёхфазного режима.
 *
 * На каждый аудиоблок считает контрольные точки (alpha, beta) с шагом [STEP]
 * сэмплов — их линейно интерполирует нативный рендер. Одна точка на блок дала
 * бы ступеньки: блоки у пампа до ~85 мс.
 *
 * [fillPoints] зовёт аудиопоток, аллокаций там нет (кроме роста [points] при
 * первом блоке большего размера). [k] пишет наблюдатель калибровки,
 * [displayAlpha]/[displayBeta] читает UI.
 */
class ThreePhaseEngine(
    val patterns: ThreePhasePatternSet = ThreePhasePatternSet(),
    private val smoothingTau: Double = RESTIM_SMOOTHING_TAU,
) {

    companion object {
        /** Сэмплов на контрольную точку: ≥ 1.5 кГц при 48 кГц. */
        const val STEP = 32

        /** Переход между паттернами, с реального времени. */
        const val PATTERN_CROSSFADE_SEC = 0.3

        /**
         * Сглаживание позиции как у оси restim: 60 раз в секунду новое значение
         * догоняется линейной рампой за 1/30 с, то есть за 1/60 с проходится
         * половина пути — это первый порядок с τ = 1 / (60 · ln 2) ≈ 24 мс.
         */
        val RESTIM_SMOOTHING_TAU = 1.0 / (60 * ln(2.0))
    }

    /** Матрица калибровки [ThreePhaseMath.channelMatrix], подменяется целиком. */
    @Volatile
    var k: FloatArray = ThreePhaseMath.channelMatrix(0f, 0f, 0f)

    /** Последняя выданная позиция — для точки на экране. */
    @Volatile
    var displayAlpha = 0f
        private set

    @Volatile
    var displayBeta = 0f
        private set

    /** Контрольные точки блока `[a0, b0, a1, b1, …]`, действительны до следующего вызова. */
    var points = FloatArray(0)
        private set

    private var current: ThreePhasePattern? = null
    private var currentName: String? = null

    private var lastAlpha = 0f
    private var lastBeta = 0f

    //Переход между паттернами: из точки (fromAlpha, fromBeta) в новый паттерн
    private var fromAlpha = 0f
    private var fromBeta = 0f
    private var fadeElapsed = PATTERN_CROSSFADE_SEC

    //Калибровка прошлого блока — для рампы, см. blockParams
    private var prevK: FloatArray? = null
    private var prevCenter = Float.NaN
    private val params = FloatArray(10)

    /**
     * Параметры блока для нативного рендера:
     * `[K₀ 4 шт., K₁ 4 шт., центр₀, центр₁]` — от значений прошлого блока к
     * текущим. Натив рампирует их по блоку, и смена калибровки не щёлкает.
     * Массив переиспользуется, действителен до следующего вызова.
     */
    fun blockParams(centerReductionPct: Float): FloatArray {
        val kNow = k
        val kFrom = prevK ?: kNow
        for (i in 0..3) {
            params[i] = kFrom[i]
            params[4 + i] = kNow[i]
        }
        params[8] = if (prevCenter.isNaN()) centerReductionPct else prevCenter
        params[9] = centerReductionPct
        prevK = kNow
        prevCenter = centerReductionPct
        return params
    }

    /**
     * Заполнить [points] на блок из [frames] сэмплов. Точка `j` соответствует
     * сэмплу `min(j · STEP, frames)`, точка 0 — конец прошлого блока.
     *
     * @return число точек `ceil(frames / STEP) + 1`
     */
    fun fillPoints(
        frames: Int,
        sampleRate: Int,
        patternName: String,
        velocity: Float,
        amplitude: Float,
    ): Int {
        val n = (frames + STEP - 1) / STEP + 1
        if (points.size < 2 * n) points = FloatArray(2 * n)

        val pattern = select(patternName, amplitude)
        val finger = patterns.finger

        points[0] = lastAlpha
        points[1] = lastBeta

        var prevSample = 0
        for (j in 1 until n) {
            val sample = min(j * STEP, frames)
            val dtReal = (sample - prevSample).toDouble() / sampleRate
            prevSample = sample

            //Под пальцем точка идёт за ним в реальном времени, скорость не при чём
            val dt = if (pattern === finger && finger.isTouching) dtReal else dtReal * velocity
            pattern.update(dt)

            var a = pattern.alpha * amplitude
            var b = pattern.beta * amplitude

            if (fadeElapsed < PATTERN_CROSSFADE_SEC) {
                fadeElapsed += dtReal
                val t = min(1.0, fadeElapsed / PATTERN_CROSSFADE_SEC).toFloat()
                a = fromAlpha + (a - fromAlpha) * t
                b = fromBeta + (b - fromBeta) * t
            }

            //Скачки (палец, амплитуда, рывковые паттерны) растягиваются как в restim
            if (smoothingTau > 0) {
                val k = (1 - exp(-dtReal / smoothingTau)).toFloat()
                val prevA = points[2 * (j - 1)]
                val prevB = points[2 * (j - 1) + 1]
                a = prevA + (a - prevA) * k
                b = prevB + (b - prevB) * k
            }

            points[2 * j] = a
            points[2 * j + 1] = b
        }

        lastAlpha = points[2 * (n - 1)]
        lastBeta = points[2 * (n - 1) + 1]
        displayAlpha = lastAlpha
        displayBeta = lastBeta
        return n
    }

    /**
     * Паттерн по имени. Индекс ищется только когда строка поменялась; смена
     * паттерна запускает переход от последней выданной точки.
     */
    private fun select(name: String, amplitude: Float): ThreePhasePattern {
        val selected = current
        if (selected != null && (name === currentName || name == currentName)) {
            currentName = name
            return selected
        }

        val next = patterns.all[patterns.indexOf(name)]
        currentName = name
        if (next === selected) return next

        next.reset()
        current = next

        if (selected == null) {
            //Первый блок: старт с позиции паттерна, без перехода
            lastAlpha = next.alpha * amplitude
            lastBeta = next.beta * amplitude
        } else {
            fromAlpha = lastAlpha
            fromBeta = lastBeta
            fadeElapsed = 0.0
        }
        return next
    }
}

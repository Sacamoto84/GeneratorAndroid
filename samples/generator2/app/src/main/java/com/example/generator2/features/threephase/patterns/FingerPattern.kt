package com.example.generator2.features.threephase.patterns

import java.util.concurrent.atomic.AtomicLong

/** Нарисованная пальцем траектория, время от первой точки, секунды. */
class Trajectory(val time: DoubleArray, val alpha: FloatArray, val beta: FloatArray) {
    val duration: Double get() = time.last()
}

/**
 * Ручной паттерн «Палец», аналог Mouse в restim.
 *
 * Палец на круге — точка идёт за ним, траектория записывается.
 * Отпустил — записанное крутится по кругу в исходном темпе. Траектории нет —
 * точка стоит.
 *
 * Потоки: touch* и [clear] зовёт UI-поток, [update] — аудиопоток. Живая
 * позиция пальца ходит через [AtomicLong], готовая траектория публикуется одной
 * volatile-ссылкой, аудиопоток ничего не аллоцирует.
 */
class FingerPattern : ThreePhasePattern {

    private companion object {
        /** Маркер «пальца нет»: точка стоит, где стоит. */
        val NO_FINGER = pack(Float.NaN, Float.NaN)

        fun pack(alpha: Float, beta: Float): Long =
            (alpha.toRawBits().toLong() shl 32) or (beta.toRawBits().toLong() and 0xFFFFFFFFL)

        fun unpackAlpha(v: Long) = Float.fromBits((v ushr 32).toInt())
        fun unpackBeta(v: Long) = Float.fromBits(v.toInt())
    }

    override val name = ThreePhasePatterns.FINGER

    override var alpha = 0f
        private set
    override var beta = 0f
        private set

    //─── общее для потоков ───────────────────────────────────────────────────
    private val live = AtomicLong(NO_FINGER)

    @Volatile
    private var touching = false

    @Volatile
    private var published: Trajectory? = null

    //─── только UI-поток ─────────────────────────────────────────────────────
    private val recTime = ArrayList<Double>()
    private val recAlpha = ArrayList<Float>()
    private val recBeta = ArrayList<Float>()

    //─── только аудиопоток ───────────────────────────────────────────────────
    private var playing: Trajectory? = null
    private var progress = 0.0

    val isTouching: Boolean get() = touching

    fun trajectory(): Trajectory? = published

    fun touchDown(alpha: Float, beta: Float, timeSec: Double) {
        published = null
        recTime.clear()
        recAlpha.clear()
        recBeta.clear()
        touching = true
        touchMove(alpha, beta, timeSec)
    }

    fun touchMove(alpha: Float, beta: Float, timeSec: Double) {
        live.set(pack(alpha, beta))
        recTime.add(timeSec)
        recAlpha.add(alpha)
        recBeta.add(beta)
    }

    fun touchUp() {
        touching = false
        if (recTime.size < 2) return
        val t0 = recTime.first()
        val trajectory = Trajectory(
            DoubleArray(recTime.size) { recTime[it] - t0 },
            recAlpha.toFloatArray(),
            recBeta.toFloatArray(),
        )
        if (trajectory.duration > 0) published = trajectory
    }

    /** Стереть траекторию, точка остаётся на месте. */
    fun clear() {
        published = null
        live.set(NO_FINGER)
    }

    /** Повтор с начала; траекторию не трогает — её стирает [clear]. */
    override fun reset() {
        alpha = 0f
        beta = 0f
        playing = null
        progress = 0.0
    }

    override fun update(dt: Double) {
        val trajectory = published
        if (touching || trajectory == null) {
            followFinger()
            return
        }

        if (trajectory !== playing) {
            playing = trajectory
            progress = 0.0
        }
        progress = (progress + dt).mod(trajectory.duration)
        interpolate(trajectory, progress)
    }

    /** Точка под пальцем; сглаживание общее для всех паттернов — в движке. */
    private fun followFinger() {
        val v = live.get()
        val targetAlpha = unpackAlpha(v)
        if (targetAlpha.isNaN()) return
        alpha = targetAlpha
        beta = unpackBeta(v)
    }

    private fun interpolate(trajectory: Trajectory, t: Double) {
        val time = trajectory.time
        //Линейный поиск: точек немного, а бинарный поиск ничего не выиграет
        var i = 1
        while (i < time.size - 1 && time[i] < t) i++
        val t0 = time[i - 1]
        val t1 = time[i]
        val f = if (t1 > t0) ((t - t0) / (t1 - t0)).toFloat() else 1f
        alpha = trajectory.alpha[i - 1] + (trajectory.alpha[i] - trajectory.alpha[i - 1]) * f
        beta = trajectory.beta[i - 1] + (trajectory.beta[i] - trajectory.beta[i - 1]) * f
    }
}

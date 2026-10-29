package com.example.generator2.features.threephase.patterns

/**
 * Паттерн трёхфазного режима: точка (alpha, beta) как функция времени.
 *
 * [update] вызывается в аудиопотоке с контрольной частотой, поэтому результат
 * отдаётся через поля, а не парой — аллокации там запрещены.
 */
interface ThreePhasePattern {
    val name: String

    /** Результат последнего [update], без амплитуды пользователя. */
    val alpha: Float
    val beta: Float

    /** Состояние как у нового экземпляра. */
    fun reset()

    /** Сдвинуть время на [dt] — время паттерна (реальное · скорость пользователя). */
    fun update(dt: Double)
}

/**
 * База портов restim: время считается в Double, позиция наружу во Float.
 * Наследник задаёт [resetState] и считает позицию в [update] через [emit].
 */
abstract class RestimPattern(final override val name: String) : ThreePhasePattern {

    final override var alpha = 0f
        private set
    final override var beta = 0f
        private set

    protected fun emit(a: Double, b: Double) {
        alpha = a.toFloat()
        beta = b.toFloat()
    }

    final override fun reset() {
        alpha = 0f
        beta = 0f
        resetState()
    }

    protected abstract fun resetState()
}

object ThreePhasePatterns {
    const val FINGER = "Палец"
    const val DEFAULT = "Circle"

    /** «Палец» первым, дальше паттерны restim по алфавиту. */
    val NAMES: List<String> = listOf(FINGER) + listOf(
        "Butterfly Curve",
        "Circle",
        "Deep Throb",
        "Figure 8",
        "Jerky Stroke",
        "Lightning Strike",
        "Micro Circles",
        "Orbiting Circles",
        "Random Walk",
        "Rose Curve",
        "Spirograph",
        "Tremor Circle",
        PANNING_ARC,
        PANNING_WEDGE,
        "Vertical Oscillation",
        "W Shape",
    ).sorted()
}

internal const val PANNING_ARC = "Tri-Phase panning (ᴒ)"
internal const val PANNING_WEDGE = "Tri-Phase panning (∧)"

/**
 * Все паттерны, созданные один раз: смена паттерна — смена индекса без
 * аллокаций в аудиопотоке.
 */
class ThreePhasePatternSet {

    val finger = FingerPattern()

    val all: List<ThreePhasePattern> = listOf(
        finger,
        ButterflyPattern(),
        CirclePattern(),
        DeepThrobPattern(),
        FigureEightPattern(),
        JerkyStrokePattern(),
        LightningStrikePattern(),
        MicroCirclesPattern(),
        OrbitingCirclesPattern(),
        RandomWalkPattern(),
        RoseCurvePattern(),
        SpirographPattern(),
        TremorCirclePattern(),
        PanningArcPattern(),
        PanningWedgePattern(),
        VerticalOscillationPattern(),
        WShapePattern(),
    ).sortedBy { ThreePhasePatterns.NAMES.indexOf(it.name) }

    /** Индекс по имени; неизвестное имя — [ThreePhasePatterns.DEFAULT]. */
    fun indexOf(name: String): Int {
        val i = ThreePhasePatterns.NAMES.indexOf(name)
        return if (i >= 0) i else ThreePhasePatterns.NAMES.indexOf(ThreePhasePatterns.DEFAULT)
    }
}

package com.example.generator2

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

/** Отметка частотной шкалы. Крупные подписываются, мелкие только рисуются. */
data class FrequencyTick(val frequencyHz: Float, val isMajor: Boolean)

/**
 * Отметки шкалы для видимого куска спектра.
 *
 * Чистый расчёт без канвы: сколько отметок влезает, решает ширина в пикселях.
 *
 * @param widthPx ширина области рисования, от неё зависит частота крупных отметок
 */
fun frequencyTicks(
    visibleMin: Float,
    visibleMax: Float,
    logarithmic: Boolean,
    widthPx: Int
): List<FrequencyTick> =
    if (logarithmic) logarithmicTicks(visibleMin, visibleMax)
    else linearTicks(visibleMin, visibleMax, widthPx)

private fun linearTicks(visibleMin: Float, visibleMax: Float, widthPx: Int): List<FrequencyTick> {
    if (visibleMax <= visibleMin) return emptyList()

    val targetMajorCount = (widthPx / 90f).toInt().coerceAtLeast(2)
    val majorStep = niceFrequencyStep((visibleMax - visibleMin) / targetMajorCount)
    val minorStep = majorStep / 5f
    val ticks = mutableListOf<FrequencyTick>()
    var frequency = ceil(visibleMin / minorStep) * minorStep
    while (frequency <= visibleMax) {
        val isMajor = abs((frequency / majorStep) - round(frequency / majorStep)) < 0.001f
        ticks += FrequencyTick(frequency, isMajor)
        frequency += minorStep
    }
    return ticks
}

private fun logarithmicTicks(visibleMin: Float, visibleMax: Float): List<FrequencyTick> {
    val ticks = mutableListOf<FrequencyTick>()
    var decade = 10f.pow(floor(log10(visibleMin.toDouble())).toInt())
    while (decade <= visibleMax) {
        for (multiplier in 1..9) {
            val frequency = decade * multiplier
            if (frequency in visibleMin..visibleMax) {
                ticks += FrequencyTick(frequency, multiplier == 1 || multiplier == 2 || multiplier == 5)
            }
        }
        decade *= 10f
    }
    return ticks
}

/** Округление шага до «круглого»: 1, 2, 5 или 10 в своём порядке величины. */
private fun niceFrequencyStep(value: Float): Float {
    val exponent = floor(log10(value.toDouble())).toInt()
    val power = 10f.pow(exponent)
    return when (value / power) {
        in 0f..1f -> power
        in 1f..2f -> 2f * power
        in 2f..5f -> 5f * power
        else -> 10f * power
    }
}

/** Килогерцы пишем коротко, но только когда частота ровная. */
fun formatFrequency(frequencyHz: Float): String {
    val frequency = frequencyHz.toInt()
    return if (frequency >= 1_000 && frequency % 1_000 == 0) "${frequency / 1_000}k" else "$frequency"
}

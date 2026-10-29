package com.example.generator2.features.threephase

import kotlin.math.hypot

/**
 * Круг на экране ↔ (alpha, beta), как в restim: alpha вверх (Neutral),
 * +beta влево (L+ слева снизу, R+ справа снизу).
 */
object ThreePhaseGeometry {

    fun toScreen(alpha: Float, beta: Float, cx: Float, cy: Float, radius: Float): Pair<Float, Float> =
        (cx - beta * radius) to (cy - alpha * radius)

    /** Обратное к [toScreen]; точка за кругом прижимается к краю. */
    fun fromScreen(x: Float, y: Float, cx: Float, cy: Float, radius: Float): Pair<Float, Float> {
        var alpha = (cy - y) / radius
        var beta = (cx - x) / radius
        val r = hypot(alpha, beta)
        if (r > 1f) {
            alpha /= r
            beta /= r
        }
        return alpha to beta
    }
}

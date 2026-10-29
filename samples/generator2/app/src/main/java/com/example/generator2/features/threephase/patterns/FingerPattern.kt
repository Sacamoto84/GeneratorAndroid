package com.example.generator2.features.threephase.patterns

/** Ручной паттерн «Палец». Поведение — отдельной задачей. */
class FingerPattern : ThreePhasePattern {
    override val name = ThreePhasePatterns.FINGER
    override val alpha = 0f
    override val beta = 0f
    override fun reset() {}
    override fun update(dt: Double) {}
}

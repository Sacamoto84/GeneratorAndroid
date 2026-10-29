package com.example.generator2.features.threephase.patterns

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

//Порт restim qt_ui/patterns/threephase, категория «basic»

class FigureEightPattern : RestimPattern("Figure 8") {
    private var angle = 0.0

    override fun resetState() {
        angle = 0.0
    }

    override fun update(dt: Double) {
        angle += dt
        emit(sin(angle), 0.5 * sin(2 * angle))
    }
}

class MicroCirclesPattern : RestimPattern("Micro Circles") {
    private var time = 0.0
    private val circleRadius = 0.15

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        val t = time

        //Медленный дрейф центра кружков
        val originAlpha = 0.4 * sin(t * 0.1) + 0.3 * sin(t * 0.07)
        val originBeta = 0.3 * cos(t * 0.08) + 0.2 * cos(t * 0.12)

        val sizeMod = 0.8 + 0.4 * sin(t * 0.3)
        val circleAlpha = circleRadius * cos(t * 4.0) * sizeMod
        val circleBeta = circleRadius * sin(t * 4.0) * sizeMod

        //Изредка микродрожь
        var tremorAlpha = 0.0
        var tremorBeta = 0.0
        if ((t * 2).toInt() % 7 == 0) {
            tremorAlpha = 0.02 * sin(t * 25)
            tremorBeta = 0.02 * cos(t * 27)
        }

        emit(originAlpha + circleAlpha + tremorAlpha, originBeta + circleBeta + tremorBeta)
    }
}

/** «Tri-Phase panning (ᴒ)»: дуга по краю круга. Своя скорость 0.5 как в restim. */
class PanningArcPattern : RestimPattern(PANNING_ARC) {
    private var time = 0.0
    private val velocity = 0.5

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt * velocity
        val base = sin(2.0 * PI * time) * PI * 120 / 180
        emit(cos(base), sin(base))
    }
}

/** «Tri-Phase panning (∧)»: ломаная L+ → Neutral → R+. Своя скорость 0.5 как в restim. */
class PanningWedgePattern : RestimPattern(PANNING_WEDGE) {
    private var time = 0.0
    private val velocity = 0.5

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt * velocity
        val base = sin(2.0 * PI * time)
        emit(1 - abs(base) * 1.5, sqrt(3.0) * base / 2)
    }
}

class VerticalOscillationPattern : RestimPattern("Vertical Oscillation") {
    private var time = 0.0

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        emit(sin(2 * PI * 0.5 * time), 0.2 * sin(2 * PI * 5 * time))
    }
}

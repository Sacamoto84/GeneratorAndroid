package com.example.generator2.features.threephase.patterns

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

//Порт restim qt_ui/patterns/threephase, категория «mathematical»

class CirclePattern : RestimPattern("Circle") {
    private var angle = 0.0

    override fun resetState() {
        angle = 0.0
    }

    override fun update(dt: Double) {
        angle += dt
        emit(cos(angle), sin(angle))
    }
}

class ButterflyPattern : RestimPattern("Butterfly Curve") {
    private var time = 0.0

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt * 0.5
        val t = time
        val k = exp(cos(t)) - 2 * cos(4 * t) - sin(t / 12).pow(5)
        val scale = 0.15
        emit(sin(t) * k * scale, cos(t) * k * scale)
    }
}

class RoseCurvePattern : RestimPattern("Rose Curve") {
    private var time = 0.0
    private val n = 5 //число лепестков

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        val theta = time
        val r = abs(cos(n * theta))
        emit(r * cos(theta), r * sin(theta))
    }
}

class SpirographPattern : RestimPattern("Spirograph") {
    private var time = 0.0
    private val bigR = 3.0 //радиус неподвижного круга
    private val r = 1.0    //радиус катящегося круга
    private val d = 0.7    //расстояние от центра

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        val t = time
        val alpha = (bigR + r) * cos(t) - d * cos((bigR + r) / r * t)
        val beta = (bigR + r) * sin(t) - d * sin((bigR + r) / r * t)
        val maxVal = bigR + r + d
        emit(alpha / maxVal * 0.8, beta / maxVal * 0.8)
    }
}

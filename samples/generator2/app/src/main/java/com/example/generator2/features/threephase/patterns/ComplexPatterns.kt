package com.example.generator2.features.threephase.patterns

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

//Порт restim qt_ui/patterns/threephase, категория «complex»

class JerkyStrokePattern : RestimPattern("Jerky Stroke") {
    private var time = 0.0
    private var jerkTimer = 0.0

    override fun resetState() {
        time = 0.0
        jerkTimer = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        jerkTimer += dt * 5

        //Основной ход вверх-вниз
        val strokeCycle = 3.0
        val strokeProgress = time.mod(strokeCycle) / strokeCycle
        var alpha = if (strokeProgress < 0.6) {
            (strokeProgress / 0.6).pow(0.3) * 2 - 1
        } else {
            val returnProgress = 1 - (1 - (strokeProgress - 0.6) / 0.4).pow(2)
            1 - returnProgress * 2
        }

        //Рывки
        if (jerkTimer.mod(1.0) < 0.15) alpha += sin(jerkTimer * 30) * 0.08

        val beta = 0.3 * sin(time * 1.3) + 0.1 * sin(time * 7.1)

        emit(alpha.coerceIn(-1.0, 1.0), beta.coerceIn(-1.0, 1.0))
    }
}

class OrbitingCirclesPattern : RestimPattern("Orbiting Circles") {
    private var time = 0.0

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        val t = time

        //Центр гуляет по оси alpha и задерживается у Neutral
        var originAlpha = 0.7 * sin(t * 0.4)
        val lingerPull = 0.4 * exp(-((originAlpha - 1.0).pow(2)) * 5) * (1 + sin(t * 0.15 * 10))
        originAlpha += lingerPull
        val originBeta = 0.15 * sin(t * 0.7) + 0.1 * cos(t * 1.3)

        //Кружки переменного радиуса
        val radiusVariation = 0.6 * (1 + 0.5 * sin(t * 0.8) + 0.3 * sin(t * 2.1))
        val radius = 0.3 * radiusVariation
        val alpha = originAlpha + radius * cos(t * 3.0)
        val beta = originBeta + radius * sin(t * 3.0)

        emit(alpha.coerceIn(-1.2, 1.5), beta.coerceIn(-1.0, 1.0))
    }
}

class TremorCirclePattern : RestimPattern("Tremor Circle") {
    private var time = 0.0

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        val t = time

        val circleAngle = (t / 6.0) * 2 * PI
        val baseAlpha = 0.6 * cos(circleAngle)
        val baseBeta = 0.6 * sin(circleAngle)

        val f1 = 25.0
        val f2 = 31.5
        val envelope = 0.8 + 0.2 * sin(t * 0.7)
        val tremorAlpha = 0.08 * (sin(t * f1) * 0.6 + sin(t * f2 * 1.3) * 0.4) * envelope
        val tremorBeta = 0.08 * (cos(t * f1 * 1.1) * 0.6 + cos(t * f2 * 0.9) * 0.4) * envelope

        emit(
            (baseAlpha + tremorAlpha).coerceIn(-1.0, 1.0),
            (baseBeta + tremorBeta).coerceIn(-1.0, 1.0)
        )
    }
}

class WShapePattern : RestimPattern("W Shape") {
    private var time = 0.0

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt * 2 //базовая скорость удвоена

        val cycleTime = time.mod(4 * PI)
        val phase = (cycleTime / PI).toInt()
        val progress = cycleTime.mod(PI) / PI
        val firstHalf = progress <= 0.5
        val t = if (firstHalf) progress * 2 else (progress - 0.5) * 2

        val alpha = if (firstHalf) 1 - t else t
        val beta = when (phase) {
            0 -> if (firstHalf) t * 0.5 else 0.5 + t * 0.5    //первая V вперёд
            1 -> if (firstHalf) 1 - t * 0.5 else 0.5 - t * 0.5 //первая V назад
            2 -> if (firstHalf) -t * 0.5 else -0.5 - t * 0.5   //вторая V вперёд
            else -> if (firstHalf) -1 + t * 0.5 else -0.5 + t * 0.5 //вторая V назад
        }

        emit(alpha.coerceIn(0.0, 1.0), beta.coerceIn(-1.0, 1.0))
    }
}

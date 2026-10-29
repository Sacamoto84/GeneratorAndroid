package com.example.generator2.features.threephase.patterns

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

//Порт restim qt_ui/patterns/threephase, категория «experimental»

class DeepThrobPattern : RestimPattern("Deep Throb") {
    private var time = 0.0

    override fun resetState() {
        time = 0.0
    }

    override fun update(dt: Double) {
        time += dt * 0.4 //медленный ритм
        val t = time

        //Пульсация: плавный подъём, спад
        val throbCycle = (t * 0.8).mod(2 * PI)
        val intensity = if (throbCycle < PI) {
            sin(throbCycle / PI * PI).pow(2)
        } else {
            (1.0 - (throbCycle - PI) / PI).pow(2)
        }

        //Вариация глубины на 30% цикла
        val depthCycle = (t * 0.2).mod(2 * PI)
        val depthModifier = if (depthCycle < PI * 0.3) {
            1.0 + 0.4 * sin(depthCycle / (PI * 0.3) * PI)
        } else 1.0

        var alpha = (-0.2 + 1.3 * intensity) * depthModifier
        var beta = 0.1 * sin(t * 0.6) + 0.05 * sin(t * 1.1)

        //Толчки на пике
        if (intensity > 0.8) {
            val strength = (intensity - 0.8) / 0.2
            alpha += 0.2 * strength * sin(t * 8.0)
            beta += 0.1 * strength * cos(t * 6.5)
        }

        emit(alpha.coerceIn(-0.5, 1.3), beta.coerceIn(-0.3, 0.3))
    }
}

class LightningStrikePattern : RestimPattern("Lightning Strike") {
    private var time = 0.0
    private var lastStrikeTime = 0.0
    private val strikeDuration = 0.15
    private var strikeInterval = 2.5
    private var inStrike = false

    override fun resetState() {
        time = 0.0
        lastStrikeTime = 0.0
        strikeInterval = 2.5
        inStrike = false
    }

    override fun update(dt: Double) {
        time += dt
        val t = time

        if (!inStrike && (t - lastStrikeTime) > strikeInterval) {
            inStrike = true
            lastStrikeTime = t
            //Псевдослучайный интервал до следующего удара
            val pseudoRand = abs(sin(t * 17.543)) * abs(cos(t * 23.891))
            strikeInterval = 1.0 + pseudoRand * 3.0
        }

        if (inStrike && (t - lastStrikeTime) > strikeDuration) inStrike = false

        if (inStrike) {
            val progress = (t - lastStrikeTime) / strikeDuration
            val intensity = sin(progress * PI) * 2.0
            var alpha = intensity * (0.5 + 0.8 * sin(t * 45))
            var beta = intensity * 0.4 * cos(t * 38 + PI / 3)
            if ((t * 50).toInt() % 3 == 0) alpha *= -0.7
            if ((t * 43).toInt() % 4 == 0) beta *= -0.9
            emit(alpha, beta)
        } else {
            emit(0.1 * sin(t * 0.8), 0.05 * cos(t * 0.6))
        }
    }
}

class RandomWalkPattern : RestimPattern("Random Walk") {
    private var time = 0.0
    private var alphaPos = 0.0
    private var betaPos = 0.0
    private val centerPullStrength = 0.003
    private val noiseStrength = 0.8

    override fun resetState() {
        time = 0.0
        alphaPos = 0.0
        betaPos = 0.0
    }

    override fun update(dt: Double) {
        time += dt
        val t = time

        val randX = sin(t * 37.12345) * cos(t * 23.67891)
        val randY = sin(t * 41.98765) * cos(t * 19.43210)
        alphaPos += randX * noiseStrength * dt
        betaPos += randY * noiseStrength * dt

        //В restim притяжение делается на каждый вызов при 60 вызовах в секунду,
        //здесь — то же самое по времени паттерна
        val pull = (1 - centerPullStrength).pow(dt * 60)
        alphaPos = (alphaPos * pull).coerceIn(-1.0, 1.0)
        betaPos = (betaPos * pull).coerceIn(-1.0, 1.0)

        emit(alphaPos, betaPos)
    }
}

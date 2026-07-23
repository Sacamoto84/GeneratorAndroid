package com.example.generator2.features.generator

import kotlin.math.roundToInt

// Режимы мастер-громкости (ch*_Master_Mode)
const val MASTER_MODE_SLOW = 1    // Плавный: модуляция формой
const val MASTER_MODE_ONOFF = 2   // Вкл/Выкл: гейт по двум временам
const val MASTER_MODE_BUTTON = 3  // Кнопка: общий momentary-оверрайд

/**
 * DDS-инкремент фазы для Плавного режима.
 * Период в секундах (зажим 0.1..100) -> частота 0.01..10 Гц -> прирост фазы на сэмпл.
 * Формула как в RenderChannel.convertHzToR.
 */
fun masterPeriodToR(period: Float, sampleRate: Int): Int {
    val p = period.coerceIn(0.1f, 100f)
    val freq = 1f / p
    return ((4294967296L / sampleRate) * freq).toInt()
}

/** Секунды (зажим 0.1..100) -> число сэмплов, не меньше 1. */
fun secToSamples(sec: Float, sampleRate: Int): Int =
    (sec.coerceIn(0.1f, 100f) * sampleRate).roundToInt().coerceAtLeast(1)

/**
 * Активен ли глобальный оверрайд Кнопки:
 * хотя бы один включённый канал стоит в режиме Кнопка.
 */
fun masterButtonActive(
    ch1En: Boolean, ch1Mode: Int,
    ch2En: Boolean, ch2Mode: Int
): Boolean =
    (ch1En && ch1Mode == MASTER_MODE_BUTTON) ||
    (ch2En && ch2Mode == MASTER_MODE_BUTTON)

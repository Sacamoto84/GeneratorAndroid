package com.example.generator2.features.generator

import kotlin.math.roundToInt

// Режимы метаморфозы несущей (ch*_Morph_Mode)
const val MORPH_MODE_STEP = 0    // Ступень: резкая смена формы по обороту фазы
const val MORPH_MODE_SMOOTH = 1  // Плавно: линейный морфинг текущей формы в следующую

/** Длительность одного шага в секундах (зажим 0.1..100) -> число сэмплов, не меньше 1. */
fun morphSteps(time: Float, sampleRate: Int): Int =
    (time.coerceIn(0.1f, 100f) * sampleRate).roundToInt().coerceAtLeast(1)

/** Битовая маска активных слотов: bit0 | bit1 | bit2. */
fun morphMask(slot0: Boolean, slot1: Boolean, slot2: Boolean): Int =
    (if (slot0) 1 else 0) or (if (slot1) 2 else 0) or (if (slot2) 4 else 0)

/** Метаморфоза реально работает, только если включена и есть хотя бы один активный слот. */
fun morphEffective(en: Boolean, mask: Int): Boolean = en && mask != 0

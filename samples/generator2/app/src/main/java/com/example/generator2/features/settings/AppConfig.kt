package com.example.generator2.features.settings

import kotlinx.serialization.Serializable

/**
 * ### Все настройки приложения одной структурой
 *
 * Значения по умолчанию живут здесь и только здесь.
 *
 * Добавить настройку = добавить поле со значением по умолчанию. Миграция при этом
 * не нужна: старый файл без этого поля прочитается, поле возьмёт дефолт.
 * Удалить настройку = удалить поле, лишний ключ в файле игнорируется.
 */
@Serializable
data class AppConfig(

    /** Язык интерфейса: "ru" | "en" */
    val language: String = "ru",

    /** Автообновление приложения из GitHub/S3 */
    val autoUpdate: Boolean = false,

    /** Максимальная громкость усилителя, левый канал, 0..1 */
    val maxVolume0: Float = 0.9f,

    /** Максимальная громкость усилителя, правый канал, 0..1 */
    val maxVolume1: Float = 0.9f,

    /** Чувствительность слайдера несущей */
    val sensitivitySliderCr: Float = 0.2f,

    /** Чувствительность слайдера девиации FM */
    val sensitivitySliderFmDev: Float = 0.2f,

    /** Чувствительность слайдера AM/FM */
    val sensitivitySliderAmFm: Float = 0.01f,
)

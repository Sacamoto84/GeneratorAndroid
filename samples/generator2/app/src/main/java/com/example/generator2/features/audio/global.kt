package com.example.generator2.features.audio

import kotlinx.coroutines.flow.MutableStateFlow


/**
 * Частота аудио выхода
 */
val AudioSampleRate = MutableStateFlow(0) //Частота которая используется на аудиовыводе, для UI

/**
 * Признак того что устройство поддерживает 192k
 */
var isDeviceSupport192k = false
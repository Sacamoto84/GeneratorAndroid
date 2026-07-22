package com.example.generator2.common.eventbus

import com.example.generator2.common.haptic.HapticKind
import com.example.generator2.common.snackbar.UiMessage

/**
 * Событие уровня приложения.
 *
 * Отправка — обычно через фасады [com.example.generator2.common.snackbar.SnackBar]
 * и [com.example.generator2.common.haptic.Haptic]:
 * ```kotlin
 * SnackBar.success("Пресет применён")
 * Haptic.confirm()
 * ```
 */
sealed interface Event {

    /** Показать снекбар в корне навигации */
    data class ShowSnackBar(val message: UiMessage) : Event

    /** Дать виброотклик */
    data class PerformHaptic(val kind: HapticKind) : Event

    /** Сообщение в лог, без показа пользователю */
    data class Log(val message: String) : Event
}

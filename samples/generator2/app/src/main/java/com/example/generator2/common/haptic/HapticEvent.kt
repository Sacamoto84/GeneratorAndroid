package com.example.generator2.common.haptic

import com.example.generator2.common.eventbus.Event
import com.example.generator2.common.eventbus.EventBus

/**
 * Короткий способ дать виброотклик из любого места приложения:
 * ```kotlin
 * Haptic.confirm()
 * ```
 * Событие уходит в [EventBus], исполняет его корневой экран, у которого есть
 * доступ к `LocalHapticFeedback`.
 */
object Haptic {

    /** Подтверждение, успех действия */
    fun confirm() = post(HapticKind.CONFIRM)

    /** Отказ, ошибка действия */
    fun reject() = post(HapticKind.REJECT)

    /** Переключатель → ВКЛ */
    fun toggleOn() = post(HapticKind.TOGGLE_ON)

    /** Переключатель → ВЫКЛ */
    fun toggleOff() = post(HapticKind.TOGGLE_OFF)

    /** Долгое нажатие сработало */
    fun longPress() = post(HapticKind.LONG_PRESS)

    /** Перемещение хэндла в тексте */
    fun textHandleMove() = post(HapticKind.TEXT_HANDLE_MOVE)

    /** Контекстный клик по объекту */
    fun contextClick() = post(HapticKind.CONTEXT_CLICK)

    /** Нажатие экранной клавиши */
    fun keyboardTap() = post(HapticKind.KEYBOARD_TAP)

    /** Нажатие виртуальной кнопки */
    fun virtualKey() = post(HapticKind.VIRTUAL_KEY)

    /** Завершение жеста */
    fun gestureEnd() = post(HapticKind.GESTURE_END)

    /** Жест достиг порога активации */
    fun gestureThresholdActivate() = post(HapticKind.GESTURE_THRESHOLD_ACTIVATE)

    /** Шаг по дискретным позициям, например щелчок регулятора */
    fun segmentTick() = post(HapticKind.SEGMENT_TICK)

    /** Шаг по множеству мелких позиций */
    fun segmentFrequentTick() = post(HapticKind.SEGMENT_FREQUENT_TICK)

    fun post(kind: HapticKind) {
        EventBus.postEvent(Event.PerformHaptic(kind))
    }
}

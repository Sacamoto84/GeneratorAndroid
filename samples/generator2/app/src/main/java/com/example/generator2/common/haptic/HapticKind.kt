package com.example.generator2.common.haptic

/**
 * Виды виброотклика. Свой enum, а не `HapticFeedbackType`, чтобы событие можно было
 * послать из слоя features, не таща туда зависимость от compose-ui.
 *
 * Маппинг в тип Compose живёт в UI: `screens/root/HapticFeedbackMapping.kt`.
 */
enum class HapticKind {

    /** Подтверждение, успех действия */
    CONFIRM,

    /** Отказ, ошибка действия */
    REJECT,

    /** Переключатель → ВКЛ */
    TOGGLE_ON,

    /** Переключатель → ВЫКЛ */
    TOGGLE_OFF,

    /** Долгое нажатие сработало */
    LONG_PRESS,

    /** Перемещение хэндла в тексте */
    TEXT_HANDLE_MOVE,

    /** Контекстный клик по объекту */
    CONTEXT_CLICK,

    /** Нажатие экранной клавиши */
    KEYBOARD_TAP,

    /** Нажатие виртуальной кнопки */
    VIRTUAL_KEY,

    /** Завершение жеста */
    GESTURE_END,

    /** Жест достиг порога активации */
    GESTURE_THRESHOLD_ACTIVATE,

    /** Шаг по дискретным позициям */
    SEGMENT_TICK,

    /** Шаг по множеству мелких позиций */
    SEGMENT_FREQUENT_TICK
}

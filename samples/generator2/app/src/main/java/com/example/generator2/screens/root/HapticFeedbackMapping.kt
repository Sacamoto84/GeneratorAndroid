package com.example.generator2.screens.root

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.generator2.common.haptic.HapticKind

/**
 * Перевод [HapticKind] в тип Compose. Держится в UI-слое, чтобы common и features
 * не зависели от compose-ui.
 */
fun HapticKind.toComposeType(): HapticFeedbackType = when (this) {
    HapticKind.CONFIRM -> HapticFeedbackType.Confirm
    HapticKind.REJECT -> HapticFeedbackType.Reject
    HapticKind.TOGGLE_ON -> HapticFeedbackType.ToggleOn
    HapticKind.TOGGLE_OFF -> HapticFeedbackType.ToggleOff
    HapticKind.LONG_PRESS -> HapticFeedbackType.LongPress
    HapticKind.TEXT_HANDLE_MOVE -> HapticFeedbackType.TextHandleMove
    HapticKind.CONTEXT_CLICK -> HapticFeedbackType.ContextClick
    HapticKind.KEYBOARD_TAP -> HapticFeedbackType.KeyboardTap
    HapticKind.VIRTUAL_KEY -> HapticFeedbackType.VirtualKey
    HapticKind.GESTURE_END -> HapticFeedbackType.GestureEnd
    HapticKind.GESTURE_THRESHOLD_ACTIVATE -> HapticFeedbackType.GestureThresholdActivate
    HapticKind.SEGMENT_TICK -> HapticFeedbackType.SegmentTick
    HapticKind.SEGMENT_FREQUENT_TICK -> HapticFeedbackType.SegmentFrequentTick
}

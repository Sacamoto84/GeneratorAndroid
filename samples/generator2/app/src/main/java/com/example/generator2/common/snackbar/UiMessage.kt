package com.example.generator2.common.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals

/**
 * Сообщение пользователю. Тип задаёт оформление и длительность показа.
 */
sealed interface UiMessage {

    val text: String

    data class Info(override val text: String) : UiMessage
    data class Success(override val text: String) : UiMessage
    data class Error(override val text: String) : UiMessage
    data class Warning(override val text: String) : UiMessage
}

/**
 * Протаскивает [UiMessage] до отрисовки: SnackbarData отдаёт только visuals
 */
class UiSnackbarVisuals(
    val ui: UiMessage,
    override val message: String = ui.text,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration =
        if (ui is UiMessage.Error) SnackbarDuration.Long else SnackbarDuration.Short,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals

suspend fun SnackbarHostState.show(ui: UiMessage) {
    showSnackbar(UiSnackbarVisuals(ui))
}

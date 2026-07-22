package com.example.generator2.screens.root

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.example.generator2.common.snackbar.RenderSnackBarFilled
import com.example.generator2.common.snackbar.UiMessage
import com.example.generator2.common.snackbar.UiSnackbarVisuals
import kotlinx.coroutines.delay

/**
 * Единственный на приложение хост снекбара. Оформление живёт в [RenderSnackBarFilled],
 * здесь только время показа и извлечение типа сообщения из visuals.
 */
@Composable
fun RootSnackbarHost(snackBarHostState: SnackbarHostState) {

    Box(modifier = Modifier.zIndex(Float.MAX_VALUE)) {

        SnackbarHost(snackBarHostState) { data ->

            val uiMsg = (data.visuals as? UiSnackbarVisuals)?.ui
                ?: UiMessage.Info(data.visuals.message)

            LaunchedEffect(data) {
                delay(if (uiMsg is UiMessage.Error) 5000L else 2000L)
                data.dismiss()
            }

            RenderSnackBarFilled(uiMsg)
        }
    }
}

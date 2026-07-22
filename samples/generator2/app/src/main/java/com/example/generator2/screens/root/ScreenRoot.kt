package com.example.generator2.screens.root

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import cafe.adriel.voyager.navigator.Navigator
import com.example.generator2.AppScreen
import com.example.generator2.common.eventbus.Event
import com.example.generator2.common.eventbus.EventBus
import com.example.generator2.common.haptic.HapticKind
import com.example.generator2.common.snackbar.UiMessage
import com.example.generator2.common.snackbar.show

/**
 * Корень UI: стек навигации Voyager, единственный на всё приложение снекбар
 * и исполнение виброоткликов.
 *
 * Любой слой шлёт событие в [EventBus] (обычно через фасады `SnackBar` и `Haptic`),
 * показывает его этот экран — так features не тянет за собой зависимости от UI.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenRoot() {

    val snackBarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        EventBus.events.collect { event ->
            when (event) {

                is Event.ShowSnackBar -> {
                    //Сообщение сопровождается откликом по своему смыслу
                    haptic.performHapticFeedback(
                        when (event.message) {
                            is UiMessage.Success -> HapticKind.CONFIRM
                            is UiMessage.Error -> HapticKind.REJECT
                            else -> HapticKind.CONTEXT_CLICK
                        }.toComposeType()
                    )
                    snackBarHostState.show(event.message)
                }

                is Event.PerformHaptic -> haptic.performHapticFeedback(event.kind.toComposeType())

                is Event.Log -> Unit
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { RootSnackbarHost(snackBarHostState) }
    ) {
        Navigator(AppScreen.Home)
    }
}

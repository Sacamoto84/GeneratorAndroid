package com.example.generator2.common.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Шина событий уровня приложения. Отправлять можно откуда угодно, включая слой features,
 * который про UI ничего не знает.
 *
 * Отправка:
 * ```kotlin
 * EventBus.postEvent(Event.ShowSnackBar(UiMessage.Success("Готово")))
 * ```
 *
 * Подписка:
 * ```kotlin
 * scope.launch {
 *     EventBus.events.collect { event -> ... }
 * }
 * ```
 */
object EventBus {

    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 1024
    )

    val events = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun postEvent(event: Event) {
        //Щелчки виброотклика летят пачками во время перетаскивания регулятора,
        //поэтому сначала пробуем положить в буфер без корутины
        if (_events.tryEmit(event)) return
        scope.launch { _events.emit(event) }
    }
}

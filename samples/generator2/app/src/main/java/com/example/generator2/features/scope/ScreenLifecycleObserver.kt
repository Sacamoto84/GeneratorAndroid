package com.example.generator2.features.scope

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/** Ставит рендер на паузу вместе с экраном, на котором он живёт. */
class ScreenLifecycleObserver(
    private val onPauseAction: () -> Unit,
    private val onResumeAction: () -> Unit
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        onPauseAction()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        onResumeAction()
    }
}

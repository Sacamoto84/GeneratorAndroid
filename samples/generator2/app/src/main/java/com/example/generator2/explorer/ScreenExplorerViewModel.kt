package com.example.generator2.explorer

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.generator2.di.Hub
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@HiltViewModel
class ScreenExplorerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val hub: Hub
) : ViewModel() {









}
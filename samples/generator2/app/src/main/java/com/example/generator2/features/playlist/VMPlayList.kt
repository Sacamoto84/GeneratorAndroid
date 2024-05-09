package com.example.generator2.features.playlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class VMPlayList @Inject constructor(
    val playlist: Playlist,
) : ViewModel() {

    init {
        Timber.i("VMPlayList Dao")
    }


}
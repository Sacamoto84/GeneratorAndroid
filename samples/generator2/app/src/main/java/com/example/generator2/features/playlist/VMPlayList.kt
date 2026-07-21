package com.example.generator2.features.playlist

import cafe.adriel.voyager.core.model.ScreenModel
import timber.log.Timber
import javax.inject.Inject


class VMPlayList @Inject constructor(
    val playlist: Playlist,
) : ScreenModel {

    init {
        Timber.i("VMPlayList Dao")
    }


}
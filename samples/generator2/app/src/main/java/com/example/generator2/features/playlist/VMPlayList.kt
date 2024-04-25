package com.example.generator2.features.playlist

import androidx.lifecycle.ViewModel
import com.example.generator2.room.AppDatabase
import com.example.generator2.room.EntityPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class VMPlayList @Inject constructor(
    val playlist: Playlist,
    val db: AppDatabase
) : ViewModel() {

    init {
        Timber.i("VMPlayList Dao")
        db.maindao().insert(EntityPlaylist(0, "qww"))
    }


}
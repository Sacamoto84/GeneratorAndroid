package com.example.generator2.di

import android.content.Context
import com.example.generator2.PlaybackEngine
import com.example.generator2.audio_device.AudioDevice
import com.example.generator2.backup.Backup
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import com.example.generator2.vm.Script
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object HomeActivityModule {

    @Provides
    @Singleton
    fun provideUtilsKT(
        @ApplicationContext context: Context, playbackEngine: PlaybackEngine
    ): UtilsKT {
        Timber.i("..DI provideUtilsKT()")
        return UtilsKT(context, playbackEngine)
    }

    @Provides
    @Singleton
    fun provideScript(): Script {
        Timber.i("..DI provideScript()")
        return Script()
    }

    @Provides
    @Singleton
    fun provideKeyboard(script: Script): ScriptKeyboard {
        Timber.i("..DI provideKeyboard()")
        return ScriptKeyboard(script)
    }

    @Provides
    @Singleton
    fun providePlaybackEngine(@ApplicationContext context: Context): PlaybackEngine {
        Timber.i("..DI providePlaybackEngine()")
        return PlaybackEngine(context)
    }

    @Provides
    @Singleton
    fun provideAudioDevice(
        @ApplicationContext context: Context,
        playbackEngine: PlaybackEngine,
        utils: UtilsKT,
    ): AudioDevice {
        Timber.i("..DI provideAudioDevice()")
        return AudioDevice(context, playbackEngine, utils)
    }

    @Provides
    @Singleton
    fun provideBackup(@ApplicationContext context: Context): Backup {
        Timber.i("..DI provideBackup()")
        return Backup(context)
    }

    @Provides
    @Singleton
    fun provideHub(
        utils: UtilsKT,
        script: Script,
        keyboard: ScriptKeyboard,
        playbackEngine: PlaybackEngine,
        audioDevice: AudioDevice,
        backup: Backup
    ): Hub {

        Timber.i("..DI Hub()")

        return Hub(
            utils = utils,
            script = script,
            keyboard = keyboard,
            playbackEngine = playbackEngine,
            audioDevice = audioDevice,
            backup = backup
        )
    }


}
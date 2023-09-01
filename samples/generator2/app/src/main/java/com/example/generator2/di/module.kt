package com.example.generator2.di

import android.content.Context
import com.example.generator2.backup.Backup
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import com.example.generator2.element.Script
import com.example.generator2.exoplayer
import com.example.generator2.mp3.PlayerMP3
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

    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    fun providePlayerMP3(
        @ApplicationContext context: Context
    ): PlayerMP3 {
        Timber.i("..DI PlayerMP3()")
        exoplayer = PlayerMP3(context)
        return exoplayer
    }


    @Provides
    @Singleton
    fun provideUtilsKT(
        @ApplicationContext context: Context
    ): UtilsKT {
        Timber.i("..DI provideUtilsKT()")
        return UtilsKT(context)
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
        backup: Backup,
        mp3: PlayerMP3
    ): Hub {

        Timber.i("..DI Hub()")

        return Hub(
            utils = utils,
            script = script,
            keyboard = keyboard,
            backup = backup,
            mp3 = mp3
        )
    }


}
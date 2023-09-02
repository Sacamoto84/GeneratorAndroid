package com.example.generator2.di

import android.content.Context
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.element.Script
import com.example.generator2.generator.Generator
import com.example.generator2.mp3.PlayerMP3
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
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
    fun provideAudioMixerPump(
        gen: Generator,
        exoplayer : PlayerMP3
    ): AudioMixerPump {
        Timber.i("..DI AudioMixerPump()")
        return AudioMixerPump(gen, exoplayer)
    }


    @Provides
    @Singleton
    fun provideGen(): Generator {
        Timber.i("..DI Generator()")
        return Generator()
    }


    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    fun providePlayerMP3(
        @ApplicationContext context: Context
    ): PlayerMP3 {
        Timber.i("..DI PlayerMP3()")
        return PlayerMP3(context)
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
    fun provideScript(
        gen: Generator,
    ): Script {
        Timber.i("..DI provideScript()")
        return Script(gen)
    }

    @Provides
    @Singleton
    fun provideKeyboard(
        script: Script,
        gen: Generator
    ): ScriptKeyboard {
        Timber.i("..DI provideKeyboard()")
        return ScriptKeyboard(script, gen)
    }

}
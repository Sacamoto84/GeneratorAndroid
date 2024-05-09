package com.example.generator2.di

import android.content.Context
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.audio.AudioMixerPump
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.initialization.Initialization
import com.example.generator2.features.mp3.PlayerMP3
import com.example.generator2.features.scope.Scope
import com.example.generator2.features.script.Script
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeActivityModule {

    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    fun provideAudioMixerPump(
        gen: Generator,
        exoplayer: PlayerMP3,
        scope: Scope
    ): AudioMixerPump {
        println("..DI AudioMixerPump()")
        return AudioMixerPump(gen, exoplayer, scope)
    }


    @Provides
    @Singleton
    fun provideGen(): Generator {
        println("..DI Generator()")
        return Generator()
    }

    @Provides
    @Singleton
    fun provideScope(): Scope {
        println("..DI Scope()")
        return Scope()
    }


    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    fun providePlayerMP3(
        @ApplicationContext context: Context,
        scope: Scope
    ): PlayerMP3 {
        println("..DI PlayerMP3()")
        return PlayerMP3(context, scope)
    }


    @Provides
    @Singleton
    fun provideUtilsKT(
        @ApplicationContext context: Context
    ): UtilsKT {
        println("..DI provideUtilsKT()")
        return UtilsKT(context)
    }

    @Provides
    @Singleton
    fun provideScript(
        gen: Generator,
    ): Script {
        println("..DI provideScript()")
        return Script(gen)
    }

    @Provides
    @Singleton
    fun provideKeyboard(
        script: Script,
        gen: Generator
    ): ScriptKeyboard {
        println("..DI provideKeyboard()")
        return ScriptKeyboard(script, gen)
    }

    @Provides
    @Singleton
    fun providePath(@ApplicationContext context: Context): AppPath {
        println("..DI providePath()")
        return AppPath(context)
    }

    @Provides
    @Singleton
    fun provideInitialization(
        @ApplicationContext context: Context,
        gen: Generator,
        utils: UtilsKT,
        appPath: AppPath,
        global: Global
    ): Initialization {

        println("..DI provideInitialization()")

        return Initialization(
            context = context,
            gen = gen,
            utils = utils,
            appPath = appPath,
            global = global
        )
    }

}
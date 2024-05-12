package com.example.generator2.di

import android.content.Context
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.features.audio.AudioMixerPump
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
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeActivityModule {

    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    fun provideAudioMixerPump(
        @ApplicationContext context: Context
    ): AudioMixerPump {
        Timber.tag("Время работы").i("..DI AudioMixerPump()")
        return AudioMixerPump(context)
    }






    @Provides
    @Singleton
    fun provideUtilsKT(
        @ApplicationContext context: Context
    ): UtilsKT {
        Timber.tag("Время работы").i("..DI provideUtilsKT()")
        return UtilsKT(context)
    }

//    @Provides
//    @Singleton
//    fun provideScript(
//        gen: Generator,
//    ): Script {
//        Timber.tag("Время работы").i("..DI provideScript()")
//        return Script(gen)
//    }

//    @Provides
//    @Singleton
//    fun provideKeyboard(
//        script: Script,
//        gen: Generator
//    ): ScriptKeyboard {
//        Timber.tag("Время работы").i("..DI provideKeyboard()")
//        return ScriptKeyboard(script, gen)
//    }

    @Provides
    @Singleton
    fun providePath(@ApplicationContext context: Context): AppPath {
        Timber.tag("Время работы").i("..DI providePath()")
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

        Timber.tag("Время работы").i("..DI provideInitialization()")

        return Initialization(
            context = context,
            gen = gen,
            utils = utils,
            appPath = appPath,
            global = global
        )
    }

}
package com.example.generator2.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.initialization.Initialization
import com.example.generator2.features.script.Script
import com.example.generator2.screens.scripting.ui.ScriptKeyboard
import com.example.generator2.util.UtilsKT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainAudioMixerPump

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PreviewAudioMixerPump


@Module
@InstallIn(SingletonComponent::class)
object HomeActivityModule {

    @UnstableApi
    @Provides
    @Singleton
    @MainAudioMixerPump
    fun provideAudioMixerPump(
        @ApplicationContext context: Context,
        gen: Generator
    ): AudioMixerPump {
        Timber.tag("Время работы").i("..DI AudioMixerPump() main")
        return AudioMixerPump(context, gen)
    }

    @UnstableApi
    @Provides
    @Singleton
    @PreviewAudioMixerPump
    fun providePreviewAudioMixerPump(
        @ApplicationContext context: Context,
        gen: Generator
    ): AudioMixerPump {
        Timber.tag("Время работы").i("..DI AudioMixerPump() preview")
        return AudioMixerPump(context, gen)
    }


    @Provides
    @Singleton
    fun provideGenerator(): Generator {
        Timber.tag("Время работы").i("..DI provideGenerator()")
        return Generator()
    }


    @Provides
    @Singleton
    fun provideUtilsKT(
        @ApplicationContext context: Context
    ): UtilsKT {
        Timber.tag("Время работы").i("..DI provideUtilsKT()")
        return UtilsKT(context)
    }

    @Provides
    @Singleton
    fun provideScript(
        gen: Generator,
    ): Script {
        Timber.tag("Время работы").i("..DI provideScript()")
        return Script(gen)
    }

    @Provides
    @Singleton
    fun provideKeyboard(
        script: Script,
        gen: Generator
    ): ScriptKeyboard {
        Timber.tag("Время работы").i("..DI provideKeyboard()")
        return ScriptKeyboard(script, gen)
    }

    @Provides
    @Singleton
    fun providePath(@ApplicationContext context: Context): AppPath {
        Timber.tag("Время работы").i("..DI providePath()")
        return AppPath(context)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideInitialization(
        @ApplicationContext context: Context,
        utils: UtilsKT,
        appPath: AppPath,
        global: Global,
        @MainAudioMixerPump
        audioMixerPump: AudioMixerPump,
        @PreviewAudioMixerPump
        audioPreviewMixerPump: AudioMixerPump,
    ): Initialization {

        Timber.tag("Время работы").i("..DI provideInitialization()")

        return Initialization(
            context = context,
            utils = utils,
            appPath = appPath,
            global = global,
            audioMixerPump = audioMixerPump,
            audioPreviewMixerPump = audioPreviewMixerPump
        )
    }

}
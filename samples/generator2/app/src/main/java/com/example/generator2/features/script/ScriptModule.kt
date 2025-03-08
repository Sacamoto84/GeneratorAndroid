package com.example.generator2.features.script

import com.example.generator2.AppPath
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ScriptModule {

    @Provides
    @Singleton
    fun provideScriptUtils(
        appPath: AppPath
    ): ScriptUtils {
        Timber.tag("ScriptModule").i("..DI provideUtilsKT()")
        return ScriptUtils(appPath)
    }

}
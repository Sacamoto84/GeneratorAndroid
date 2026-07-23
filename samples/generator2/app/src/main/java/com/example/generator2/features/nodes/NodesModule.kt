package com.example.generator2.features.nodes

import com.example.generator2.AppPath
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.script.Script
import com.example.generator2.features.script.StateCommandScript
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NodesModule {

    /**
     * Обработчик остановки текстового скрипта вешаем здесь, а не в его
     * ScreenModel: Script — синглтон и продолжает крутиться, даже когда
     * экран скриптов закрыт, а ScreenModel к этому времени уже мёртв.
     */
    @Provides
    @Singleton
    fun provideGeneratorArbiter(script: Script): GeneratorArbiter =
        GeneratorArbiter().apply {
            register(RunOwner.SCRIPT) { script.command(StateCommandScript.STOP) }
        }

    @Provides
    @Singleton
    fun provideNodeGraphUtils(appPath: AppPath): NodeGraphUtils = NodeGraphUtils(appPath)

    @Provides
    @Singleton
    fun provideNodeRunner(gen: Generator, arbiter: GeneratorArbiter): NodeRunner =
        NodeRunner(gen, arbiter)
}

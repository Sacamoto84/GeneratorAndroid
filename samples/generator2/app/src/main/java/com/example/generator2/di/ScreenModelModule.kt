package com.example.generator2.di

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.hilt.ScreenModelKey
import com.example.generator2.features.explorer.presenter.ScreenExplorerViewModel
import com.example.generator2.features.playlist.VMPlayList
import com.example.generator2.features.presets.presetsVM
import com.example.generator2.screens.config.vm.VMConfig
import com.example.generator2.screens.mainscreen4.VMMain4
import com.example.generator2.screens.scripting.vm.VMScripting
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap

/**
 * Регистрация ScreenModel'ей в Hilt.
 *
 * voyager-hilt читает их через свой ScreenModelEntryPoint, который живёт
 * в ActivityComponent — поэтому модуль ставится туда же. Без записи в этой
 * карте getScreenModel<T>() упадёт в рантайме, а не на компиляции.
 */
@androidx.media3.common.util.UnstableApi
@Module
@InstallIn(ActivityComponent::class)
abstract class ScreenModelModule {

    @Binds
    @IntoMap
    @ScreenModelKey(VMMain4::class)
    abstract fun bindVMMain4(screenModel: VMMain4): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(VMConfig::class)
    abstract fun bindVMConfig(screenModel: VMConfig): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(VMScripting::class)
    abstract fun bindVMScripting(screenModel: VMScripting): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(ScreenExplorerViewModel::class)
    abstract fun bindScreenExplorerViewModel(screenModel: ScreenExplorerViewModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(presetsVM::class)
    abstract fun bindPresetsVM(screenModel: presetsVM): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(VMPlayList::class)
    abstract fun bindVMPlayList(screenModel: VMPlayList): ScreenModel
}

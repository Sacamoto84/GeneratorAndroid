package com.example.generator2

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import cafe.adriel.voyager.core.screen.Screen
import com.example.generator2.features.explorer.presenter.ScreenExplorerViewModel
import com.example.generator2.features.explorer.presenter.compose.ScreenExplorer
import com.example.generator2.features.playlist.compose.PlaylistUI
import com.example.generator2.features.presets.ui.DialogPresets
import com.example.generator2.screens.config.ScreenConfig
import com.example.generator2.screens.config.vm.VMConfig
import com.example.generator2.screens.editor.ScreenEditor
import com.example.generator2.screens.html.Html
import com.example.generator2.screens.mainscreen4.Mainsreen4
import com.example.generator2.screens.mainscreen4.VMMain4
import com.example.generator2.screens.scripting.ScreenScriptCommon
import com.example.generator2.screens.scripting.ScreenScriptInfo

sealed class AppScreen : Screen {


    data object Home : AppScreen() {
        private fun readResolve(): Any = Home

        @Composable
        override fun Content() {
            val viewModel: VMMain4 = hiltViewModel()
            Mainsreen4(viewModel)
        }
    }

    data object Config :  AppScreen() {
        private fun readResolve(): Any = Config

        @Composable
        override fun Content() {
            val viewModel: VMConfig = hiltViewModel()
            ScreenConfig(viewModel)
        }
    }

    data object Explorer :  AppScreen() {
        private fun readResolve(): Any = Explorer

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            val viewModel: ScreenExplorerViewModel = hiltViewModel()
            ScreenExplorer(viewModel)
        }
    }

    data object Script :  AppScreen() {
        private fun readResolve(): Any = Script

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            ScreenScriptCommon()
        }
    }

    data object Editor :  AppScreen() {
        private fun readResolve(): Any = Editor

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            ScreenEditor()
        }
    }

    data object ScriptInfo :  AppScreen() {
        private fun readResolve(): Any = ScriptInfo

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            ScreenScriptInfo()
        }
    }

    data object Presets :  AppScreen() {
        private fun readResolve(): Any = Presets

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            DialogPresets()
        }
    }

    data object html :  AppScreen() {
        private fun readResolve(): Any = html

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            Html()
        }
    }

    data object Playlist :  AppScreen() {
        private fun readResolve(): Any = Playlist

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            PlaylistUI()
        }
    }

}








//enum class NavigationRoute(val value: String) {
//    HOME("home"),
//    SCRIPT("script"),
//    EDITOR("editor"),
//    SCRIPTINFO("scriptinfo"),
//    CONFIG("config"),
//    PRESETS("presets"),
//    HTML("html"),
//    EXPLORER("explorer"),
//    PLAYLIST("playlist"),
//}
//


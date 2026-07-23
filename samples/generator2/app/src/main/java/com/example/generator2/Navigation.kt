package com.example.generator2

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import com.example.generator2.features.explorer.presenter.ScreenExplorerViewModel
import com.example.generator2.features.explorer.presenter.compose.ScreenExplorer
import com.example.generator2.features.playlist.VMPlayList
import com.example.generator2.features.playlist.compose.PlaylistUI
import com.example.generator2.features.presets.presetsVM
import com.example.generator2.features.presets.ui.DialogPresets
import com.example.generator2.screens.config.ScreenConfig
import com.example.generator2.screens.config.vm.VMConfig
import com.example.generator2.screens.editor.ScreenEditor
import com.example.generator2.screens.html.ScreenHtml
import com.example.generator2.screens.mainscreen4.Mainsreen4
import com.example.generator2.screens.mainscreen4.VMMain4
import com.example.generator2.screens.nodes.ScreenNodes
import com.example.generator2.screens.nodes.vm.VMNodes
import com.example.generator2.screens.scripting.ScreenScriptCommon
import com.example.generator2.screens.scripting.ScreenScriptInfo
import com.example.generator2.screens.scripting.vm.VMScripting

/**
 * Экраны приложения. Навигация целиком на Voyager.
 *
 * ScreenModel берётся только здесь, через getScreenModel() из voyager-hilt:
 * это extension на Screen, поэтому доступен лишь внутри Content().
 * Вложенные composable получают модель параметром, а не достают её сами.
 * Каждый ScreenModel живёт ровно столько, сколько его Screen в стеке.
 */
sealed class AppScreen : Screen {

    data object Home : AppScreen() {
        private fun readResolve(): Any = Home

        @Composable
        override fun Content() {
            val screenModel: VMMain4 = getScreenModel()
            //Диалог нового пресета открывается из главного экрана
            val presetsScreenModel: presetsVM = getScreenModel()
            Mainsreen4(screenModel, presetsScreenModel)
        }
    }

    data object Config :  AppScreen() {
        private fun readResolve(): Any = Config

        @Composable
        override fun Content() {
            val screenModel: VMConfig = getScreenModel()
            ScreenConfig(screenModel)
        }
    }

    data object Explorer :  AppScreen() {
        private fun readResolve(): Any = Explorer

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            val screenModel: ScreenExplorerViewModel = getScreenModel()
            ScreenExplorer(screenModel)
        }
    }

    data object Script :  AppScreen() {
        private fun readResolve(): Any = Script

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            val screenModel: VMScripting = getScreenModel()
            ScreenScriptCommon(screenModel)
        }
    }

    data object Editor :  AppScreen() {
        private fun readResolve(): Any = Editor

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            val screenModel: VMMain4 = getScreenModel()
            ScreenEditor(screenModel)
        }
    }

    data object Nodes : AppScreen() {
        private fun readResolve(): Any = Nodes

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            val screenModel: VMNodes = getScreenModel()
            ScreenNodes(screenModel)
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
            val screenModel: presetsVM = getScreenModel()
            DialogPresets(screenModel)
        }
    }

    data object html :  AppScreen() {
        private fun readResolve(): Any = html

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            ScreenHtml()
        }
    }

    data object Playlist :  AppScreen() {
        private fun readResolve(): Any = Playlist

        @OptIn(UnstableApi::class)
        @Composable
        override fun Content() {
            val screenModel: VMPlayList = getScreenModel()
            PlaylistUI(screenModel)
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


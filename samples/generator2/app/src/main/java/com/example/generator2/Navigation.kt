package com.example.generator2

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.generator2.features.explorer.presenter.compose.ScreenExplorer
import com.example.generator2.features.playlist.compose.PlaylistUI
import com.example.generator2.features.presets.ui.DialogPresets
import com.example.generator2.screens.config.ScreenConfig
import com.example.generator2.screens.editor.ScreenEditor
import com.example.generator2.screens.html.Html
import com.example.generator2.screens.mainscreen4.Mainsreen4
import com.example.generator2.screens.scripting.ScreenScriptCommon
import com.example.generator2.screens.scripting.ScreenScriptInfo


enum class NavigationRoute(val value: String) {
    HOME("home"),
    SCRIPT("script"),
    EDITOR("editor"),
    SCRIPTINFO("scriptinfo"),
    CONFIG("config"),
    PRESETS("presets"),
    HTML("html"),
    EXPLORER("explorer"),
    PLAYLIST("playlist"),
}

@SuppressLint("StaticFieldLeak")
lateinit var navController: NavHostController

@androidx.media3.common.util.UnstableApi
@Composable
fun Navigation() {
    navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.HOME.value,

        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) },

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
//            .semantics {
//                testTagsAsResourceId = true
//            }
    ) {

        composable(
            NavigationRoute.HOME.value,
        )
        {
            Mainsreen4()
        }

        composable("script"
        ) {
            ScreenScriptCommon()
        }

        composable("editor"
        ) {
            ScreenEditor()
        }

        composable("scriptinfo"
        ) {
            ScreenScriptInfo()
        }

        //Экран настройки программы
        composable("config"
        ) {
            ScreenConfig()
        }

        //Экран настройки программы
        composable("presets"
        ) {
            DialogPresets()
        }


        //Экран настройки программы
        composable("html"
        ) {
            Html()
        }

        composable(NavigationRoute.EXPLORER.value)
        {
            ScreenExplorer()
        }

        composable(NavigationRoute.PLAYLIST.value)
        {
            PlaylistUI()
        }


    }


}


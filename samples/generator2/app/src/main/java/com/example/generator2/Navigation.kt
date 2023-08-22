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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.generator2.explorer.Explorer
import com.example.generator2.explorer.compose.ScreenExplorer
import com.example.generator2.presets.ui.DialogPresets
import com.example.generator2.screens.config.ScreenConfig
import com.example.generator2.screens.editor.ScreenEditor
import com.example.generator2.screens.html.Html
import com.example.generator2.screens.mainscreen4.Mainsreen4
import com.example.generator2.screens.scripting.ScreenScriptCommon
import com.example.generator2.screens.scripting.ScreenScriptInfo
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

enum class NavigationRoute(val value: String) {
    HOME("home"),
    SCRIPT("script"),
    EDITOR("editor"),
    SCRIPTINFO("scriptinfo"),
    CONFIG("config"),
    PRESETS("presets"),
    HTML("html"),
    EXPLORER("explorer")
}

@SuppressLint("StaticFieldLeak")
lateinit var navController: NavHostController

@androidx.media3.common.util.UnstableApi
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Navigation() {
    navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics {
                testTagsAsResourceId = true
            }
    ) {



        composable("home",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) })
        {
            Mainsreen4()
        }

        composable("script",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {

            ScreenScriptCommon()
        }

        composable("editor",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            ScreenEditor()
        }

        composable("scriptinfo",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            ScreenScriptInfo()
        }

        //Экран настройки программы
        composable("config",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            ScreenConfig()
        }

        //Экран настройки программы
        composable("presets",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            DialogPresets()
        }


        //Экран настройки программы
        composable("html",
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
            Html()
        }

        composable(NavigationRoute.EXPLORER.value,
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) })
        {
            ScreenExplorer()
        }


    }


}


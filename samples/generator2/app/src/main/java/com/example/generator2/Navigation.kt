package com.example.generator2

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import cafe.adriel.voyager.core.screen.Screen
import com.example.generator2.screens.config.ScreenConfig
import com.example.generator2.screens.config.vm.VMConfig
import com.example.generator2.screens.mainscreen4.Mainsreen4
import com.example.generator2.screens.mainscreen4.VMMain4


sealed class AppScreen : Screen {

    data object Home : AppScreen() {
        @Composable
        override fun Content() {
            val viewModel: VMMain4 = hiltViewModel()
            Mainsreen4(viewModel)
        }
    }

    data object Config :  AppScreen() {
        @Composable
        override fun Content() {
            val viewModel: VMConfig = hiltViewModel()
            ScreenConfig(viewModel)
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
//@SuppressLint("StaticFieldLeak")
//lateinit var navController: NavHostController
//
//@androidx.media3.common.util.UnstableApi
//@Composable
//fun Navigation() {
//    navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = NavigationRoute.HOME.value,
//
//        enterTransition = { fadeIn(animationSpec = tween(0)) },
//        exitTransition = { fadeOut(animationSpec = tween(0)) },
//        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
//        popExitTransition = { fadeOut(animationSpec = tween(0)) },
//
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
////            .semantics {
////                testTagsAsResourceId = true
////            }
//    ) {
//
//        composable(
//            NavigationRoute.HOME.value,
//        )
//        {
//            Mainsreen4()
//        }
//
//        composable("script"
//        ) {
//            ScreenScriptCommon()
//        }
//
//        composable("editor"
//        ) {
//            ScreenEditor()
//        }
//
//        composable("scriptinfo"
//        ) {
//            ScreenScriptInfo()
//        }
//
////        //Экран настройки программы
////        composable("config"
////        ) {
////            ScreenConfig()
////        }
//
//        //Экран настройки программы
//        composable("presets"
//        ) {
//            DialogPresets()
//        }
//
//
//        //Экран настройки программы
//        composable("html"
//        ) {
//            Html()
//        }
//
//        composable(NavigationRoute.EXPLORER.value)
//        {
//            ScreenExplorer()
//        }
//
//        composable(NavigationRoute.PLAYLIST.value)
//        {
//            PlaylistUI()
//        }
//
//
//    }
//
//
//}


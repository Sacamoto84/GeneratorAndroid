package com.example.generator2.screens.config

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.generator2.R
import com.example.generator2.navController
import com.example.generator2.screens.config.DefScreenConfig.caption
import com.example.generator2.screens.config.molecule.ConfigLoginScreen
import com.example.generator2.theme.colorLightBackground
import com.example.generator2.screens.config.molecule.ConfigConstrain
import com.example.generator2.screens.config.molecule.ConfigVolume
import com.example.generator2.screens.config.vm.*
import com.example.generator2.update.Update
import kotlinx.coroutines.delay

val modifierGreenButton = Modifier
    .padding(8.dp)
    .fillMaxWidth()
    .height(40.dp)

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun ScreenConfig(
    vm: VMConfig = hiltViewModel()
) {

    //Выводится информация по бекап файлу
    var backupMessage by remember { mutableStateOf("1") }

    val focusManager = LocalFocusManager.current

    Scaffold(backgroundColor = colorLightBackground, bottomBar = { BottomBar() } )
    {

        Column(
            Modifier
                .fillMaxSize()
                .background(colorLightBackground)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit)
                { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {

            Divider()
            Config_header(Update.currentVersion)
            Divider()
            Divider() ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            Divider()
            Divider()
            Divider()
            ConfigConstrain(vm)
            Divider()
            ConfigVolume(vm)
            Divider()
            Spacer(modifier = Modifier.height(400.dp))
        }

    }

}

@Composable
fun Config_header(str: String) {
    Text(
        text = str,
        color = Color(0xFFFFC300),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BottomBar() {
    BottomAppBar(
        backgroundColor = colorLightBackground,
        contentColor = Color.White,
    ) {
        //Кнопка назад
        IconButton(modifier = Modifier.testTag("buttonM4ScriptGoBack"),
            onClick = { navController.popBackStack() }) {
            Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


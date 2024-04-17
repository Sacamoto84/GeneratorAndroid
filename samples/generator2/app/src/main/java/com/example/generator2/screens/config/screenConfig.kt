package com.example.generator2.screens.config

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.R
import com.example.generator2.navController
import com.example.generator2.screens.config.molecule.ConfigConstrain
import com.example.generator2.screens.config.molecule.ConfigLanguage
import com.example.generator2.screens.config.molecule.ConfigUpdate
import com.example.generator2.screens.config.molecule.ConfigVolume
import com.example.generator2.screens.config.vm.VMConfig
import com.example.generator2.theme.colorLightBackground

val modifierGreenButton = Modifier
    .padding(8.dp)
    .fillMaxWidth()
    .height(40.dp)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun ScreenConfig(
    vm: VMConfig = hiltViewModel()
) {

    vm.recompose.value

    val focusManager = LocalFocusManager.current

    Scaffold(backgroundColor = colorLightBackground, bottomBar = { BottomBar() })
    {

        Column(
            Modifier
                .fillMaxSize()
                .background(colorLightBackground)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit)
                { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            vm.recompose
            Divider()
            //Config_header(Update.currentVersion)
            ConfigUpdate(vm)
            Divider()
            ConfigLanguage(vm)
            Divider()
            Divider()
            Divider()
            Divider()
            ConfigConstrain(vm)
            Divider()
            ConfigVolume(vm)
            Divider()

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
        modifier = Modifier.fillMaxWidth(),
        fontSize = 16.sp
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


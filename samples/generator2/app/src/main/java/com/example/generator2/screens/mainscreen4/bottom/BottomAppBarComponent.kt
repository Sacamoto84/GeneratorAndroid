package com.example.generator2.screens.mainscreen4.bottom

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.generator2.R
import com.example.generator2.features.presets.Presets
import com.example.generator2.features.presets.presetsSaveFile
import com.example.generator2.features.script.Script
import com.example.generator2.screens.mainscreen4.VMMain4
import com.example.generator2.theme.colorLightBackground


//Нижняя панель с кнопками
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun M4BottomAppBarComponent(
    vm: VMMain4,
    navigateToConfig: ()->Unit = {},
    navigateToPresets: ()->Unit = {},
    navigateToScript:  ()->Unit = {},
) {

    val context = LocalContext.current
    var r: String by remember { mutableStateOf("Auto select") }

    BottomAppBar(
        backgroundColor = colorLightBackground,
        contentColor = Color.White,
        elevation = 2.dp,
        cutoutShape = CircleShape
    ) {

        //global.hub.audioDevice.getDeviceId()

        IconButton(onClick = navigateToConfig ) {
            Icon(painter = painterResource(R.drawable.line3_2), contentDescription = null)
        }

        IconButton(onClick = navigateToPresets ) {
            Icon(
                painter = painterResource(R.drawable.folder_open2), contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        //Сохранить
        IconButton(
            onClick = { }
        ) {
            Icon(
                painter = painterResource(R.drawable.save),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .combinedClickable(
                        onClick = {
                            if ((vm.audioMixerPump.gen.liveData.presetsName.value == "") || (vm.audioMixerPump.gen.liveData.presetsName.value == "default")) {
                                Presets.isOpenDialogNewFile.value = true
                            } else {
                                presetsSaveFile(vm.audioMixerPump.gen.liveData.presetsName.value, path = vm.appPath.presets, gen = vm.audioMixerPump.gen)
                            }
                        },
                        onLongClick = {
                            Presets.isOpenDialogNewFile.value = true
                        })
            )
        }

        Spacer(modifier = Modifier.weight(1f))

//        //Запуск плейлиста
//        IconButton(modifier = Modifier.testTag("playlist"),
//            onClick = { navController.navigate(NavigationRoute.PLAYLIST.value) }) {
//            Icon(painter = painterResource(R.drawable.player_memory), contentDescription = null)
//        }

//        IconButton(modifier = Modifier.testTag("edit"),
//            onClick = { navController.navigate("html") }) {
//            Icon(painter = painterResource(R.drawable.info), contentDescription = null)
//        }

        IconButton(modifier = Modifier.testTag("buttonM4GoToScript"),
            onClick = navigateToScript ) {
            Icon(painter = painterResource(R.drawable.script3), contentDescription = null)
        }

//        IconButton(modifier = Modifier.testTag("edit"),
//            onClick = { navController.navigate("editor") }) {
//            Icon(painter = painterResource(R.drawable.editor), contentDescription = null)
//        }

        Spacer(modifier = Modifier.width(8.dp))

//        val context = LocalContext.current
//        IconButton(onClick = {
//            //global.hub.backup.json.saveJsonConfig()
//            //Puffer().saveConfig()
//            //exitProcess(0)
//
//            val intent = Intent(Intent.ACTION_MAIN)
//            intent.addCategory(Intent.CATEGORY_HOME)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(context, intent, null)
//
//        }) {
//            Icon(painter = painterResource(R.drawable.close4), contentDescription = null)
//        }

        //Spacer(modifier = Modifier.weight(0.1f))

    }
}

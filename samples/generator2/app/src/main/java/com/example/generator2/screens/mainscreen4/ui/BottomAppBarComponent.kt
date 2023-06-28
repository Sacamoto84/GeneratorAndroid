package com.example.generator2.screens.mainscreen4.ui

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.generator2.R
import com.example.generator2.model.LiveData
import com.example.generator2.navController
import com.example.generator2.presets.Presets
import com.example.generator2.presets.presetsSaveFile
import com.example.generator2.screens.mainscreen4.VMMain4
import com.example.generator2.theme.colorLightBackground


//Нижняя панель с кнопками
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun M4BottomAppBarComponent(
    toggleDrawer: () -> Unit, global: VMMain4
) {
    BottomAppBar(
        backgroundColor = colorLightBackground,
        contentColor = Color.White,
        elevation = 2.dp,
        cutoutShape = CircleShape
    ) {

        global.hub.audioDevice.getDeviceId()

        IconButton(onClick = { navController.navigate("config") }) {
            Icon(painter = painterResource(R.drawable.line3_2), contentDescription = null)
        }

        //Иконка устройства
        IconButton(
            onClick = toggleDrawer
        ) {
            val id = global.hub.audioDevice.mDeviceId
            var str = "Auto select"
            global.hub.audioDevice.mDeviceAdapter.forEach {
                if (id == it.id)
                    str = it.name
            }
            val imageVector = nameToPainter(str)
            Icon(imageVector, contentDescription = null, modifier = Modifier.size(32.dp))
        }

        //Управление скриптами
//       if ((global.hub.script.state == StateCommandScript.ISRUNNING) || (global.hub.script.state == StateCommandScript.ISPAUSE)) {
//            //Пауза
//            IconButton(onClick = {
//
//                if (global.hub.script.state != StateCommandScript.ISPAUSE) global.hub.script.command(
//                    StateCommandScript.PAUSE
//                )
//                else {
//                    global.hub.script.state = StateCommandScript.ISRUNNING
//                    global.hub.script.end = false
//                }
//
//            }) {
//
//                if (global.hub.script.state != StateCommandScript.ISPAUSE)
//                    Icon(
//                        painter = painterResource(
//                            R.drawable.pause
//                        ), contentDescription = null
//                    )
//                else
//                    Icon(
//                        painter = painterResource(
//                            R.drawable.play
//                        ), contentDescription = null
//                    )
//
//            }
//        } else {
//            //Старт
//            IconButton(onClick = {
//                global.hub.script.command(StateCommandScript.START)
//            }) {
//                Icon(painter = painterResource(R.drawable.play), contentDescription = null)
//            }
//        }
//
//        //Стоп
//        IconButton(onClick = {
//            global.hub.script.command(StateCommandScript.STOP)
//        }) {
//            Icon(painter = painterResource(R.drawable.stop), contentDescription = null)
//        }


        IconButton(onClick = {
            navController.navigate("presets")
        }) {
            Icon(painter = painterResource(R.drawable.folder_open2), contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
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

                            if ((LiveData.presetsName.value == "") || (LiveData.presetsName.value == "default")) {
                                Presets.isOpenDialogNewFile.value = true
                            } else {
                                presetsSaveFile(LiveData.presetsName.value)
                            }


                        },
                        onLongClick = {
                            Presets.isOpenDialogNewFile.value = true
                        })


            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(modifier = Modifier.testTag("edit"),
            onClick = { navController.navigate("html") }) {
            Icon(painter = painterResource(R.drawable.info), contentDescription = null)
        }

//        IconButton(modifier = Modifier.testTag("buttonM4GoToScript"),
//            onClick = { navController.navigate("script") }) {
//            Icon(painter = painterResource(R.drawable.script3), contentDescription = null)
//        }

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
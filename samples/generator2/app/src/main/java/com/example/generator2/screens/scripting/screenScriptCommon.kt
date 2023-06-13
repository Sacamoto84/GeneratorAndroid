package com.example.generator2.screens.scripting

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.generator2.R
import com.example.generator2.navController
import com.example.generator2.vm.StateCommandScript
import com.example.generator2.screens.scripting.ui.RegisterViewDraw
import com.example.generator2.screens.scripting.ui.ScriptTable
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.theme.colorLightBackground


//Основной экран для скриптов
@Composable
fun ScreenScriptCommon(global: VMScripting = hiltViewModel()) {
    Column( //Modifier
        //  .recomposeHighlighter()
        //.background(Color.Cyan)
    ) {

        Box(Modifier.weight(1f)) {
            ScriptTable(vm = global)
        }

        //Блок регистров
        if (global.hub.script.state != StateCommandScript.ISEDITTING) {
            Spacer(modifier = Modifier.height(8.dp))

            RegisterViewDraw(global = global)
        }
        Spacer(modifier = Modifier.height(8.dp))

        BottomAppBar(
            backgroundColor = colorLightBackground,
            contentColor = Color.White,
        ) {


            //Кнопка назад
            IconButton( modifier = Modifier.testTag("buttonM4ScriptGoBack"),
                onClick = { navController.popBackStack() }) {
                Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))

            if ((global.hub.script.state == StateCommandScript.ISRUNNING) || (global.hub.script.state == StateCommandScript.ISPAUSE)) {

                //Пауза
                IconButton(onClick = {
                    if (global.hub.script.state != StateCommandScript.ISPAUSE) global.hub.script.command(
                        StateCommandScript.PAUSE
                    )
                    else {
                        global.hub.script.state = StateCommandScript.ISRUNNING
                        global.hub.script.end = false
                    }
                }) {
                    if (global.hub.script.state != StateCommandScript.ISPAUSE)
                        Icon(
                        painter = painterResource(
                            R.drawable.pause
                        ), contentDescription = null
                    )
                    else
                        Icon(
                            painter = painterResource(
                                R.drawable.play
                            ), contentDescription = null
                        )
                }
            } else {
                //Старт
                IconButton(onClick = {
                    global.hub.script.command(StateCommandScript.START)
                }) {
                    Icon(painter = painterResource(R.drawable.play), contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            //Стоп
            IconButton(onClick = {
                global.hub.script.command(StateCommandScript.STOP)
            }) {
                Icon(painter = painterResource(R.drawable.stop), contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.navigate("scriptinfo") }) {
                Icon(painter = painterResource(R.drawable.info4), contentDescription = null)
            }

        }

    }
}









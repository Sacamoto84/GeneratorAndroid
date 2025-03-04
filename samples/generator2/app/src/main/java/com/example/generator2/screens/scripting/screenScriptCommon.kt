package com.example.generator2.screens.scripting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.R
import com.example.generator2.features.script.StateCommandScript
import com.example.generator2.screens.scripting.ui.RegisterViewDraw
import com.example.generator2.screens.scripting.ui.ScriptTable
import com.example.generator2.screens.scripting.vm.VMScripting
import com.example.generator2.theme.colorLightBackground

//Основной экран для скриптов
@Composable
fun ScreenScriptCommon(vm: VMScripting = hiltViewModel()) {
    Column( //Modifier
        //  .recomposeHighlighter()
        //.background(Color.Cyan)
    ) {

        Box(Modifier.weight(1f)) {
            ScriptTable(vm = vm)
        }

        //Блок регистров
        if (vm.script.state != StateCommandScript.ISEDITING) {
            Spacer(modifier = Modifier.height(8.dp))
            RegisterViewDraw(global = vm)
        }
        Spacer(modifier = Modifier.height(8.dp))

        BottomAppBar(
            backgroundColor = colorLightBackground,
            contentColor = Color.White,
        ) {


            //Кнопка назад
            IconButton(modifier = Modifier.testTag("buttonM4ScriptGoBack"),
                onClick = {
                    //navController.popBackStack()
                }) {
                Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))

            if ((vm.script.state == StateCommandScript.ISRUNNING) || (vm.script.state == StateCommandScript.ISPAUSE)) {

                //Пауза
                IconButton(onClick = {

                    if (vm.script.state != StateCommandScript.ISPAUSE) vm.script.command(
                        StateCommandScript.PAUSE
                    )
                    else {
                        vm.script.state = StateCommandScript.ISRUNNING
                        vm.script.end = false
                    }

                }) {
                    if (vm.script.state != StateCommandScript.ISPAUSE)
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
                    vm.script.command(StateCommandScript.START)
                }) {
                    Icon(painter = painterResource(R.drawable.play), contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            //Стоп
            IconButton(onClick = {
                vm.script.command(StateCommandScript.STOP)
            }) {
                Icon(painter = painterResource(R.drawable.stop), contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                //navController.navigate("scriptinfo")
            }) {
                Icon(painter = painterResource(R.drawable.info4), contentDescription = null)
            }

        }

    }

}









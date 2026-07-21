package com.example.generator2.screens.scripting

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.features.script.StateCommandScript
import com.example.generator2.screens.scripting.bottom.BottomAppBarScript
import com.example.generator2.screens.scripting.ui.RegisterViewDraw
import com.example.generator2.screens.scripting.ui.ScriptTable
import com.example.generator2.screens.scripting.vm.VMScripting

//Основной экран для скриптов
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScreenScriptCommon(vm: VMScripting) {
    Scaffold(
        bottomBar = { BottomAppBarScript(vm) }
    ) {
        Column(Modifier
            .padding(paddingValues = it).fillMaxSize()) {

            //ScriptTable растягивается на всю высоту, поэтому ему нужен вес,
            //иначе блок регистров выдавливается за нижнюю границу экрана
            Box(Modifier.weight(1f)) {
                ScriptTable(vm = vm)
            }

            //Блок регистров
            if (vm.script.state != StateCommandScript.ISEDITING) {
                Spacer(modifier = Modifier.height(8.dp))
                RegisterViewDraw(vm.script.registers.collectAsStateWithLifecycle().value)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}









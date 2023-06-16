package com.example.generator2.presets.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.R
import com.example.generator2.navController
import com.example.generator2.presets.Presets
import com.example.generator2.presets.presetsGetListName
import com.example.generator2.presets.presetsVM
import com.siddroid.holi.colors.MaterialColor
import libs.modifier.scrollbar

val PresetsDialogRecompose = mutableIntStateOf(0)

@Composable
fun DialogPresets(vm: presetsVM = hiltViewModel()) {

    Presets.presetList = presetsGetListName()

    PresetsDialogRecompose

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 32.dp),
        shape = RoundedCornerShape(size = 10.dp),
        color = Color.White
    ) {

        if (Presets.isOpenDialogNewFile.collectAsState().value) {
            DialogPresetsNewFile()
        }

        if (Presets.isOpenDialogDeleteRename.collectAsState().value) {
            DialogPresetsDeleteRename(Presets.isOpenDialogDeleteRenameName)
        }

        Scaffold(
            topBar = { TopBar() },
            bottomBar = { BottomBar() }
        )
        {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = it.calculateTopPadding(),
                        bottom = it.calculateBottomPadding()
                    )
            ) {
                Content(vm)
            }
        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(vm: presetsVM) {

    val state: LazyListState = rememberLazyListState()

    val count = Presets.presetList.count { it.isNotEmpty() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 4.dp)
            .scrollbar(
                count = count,
                state = state,
                horizontal = false,
                countCorrection = 0,
                hiddenAlpha = 0f,
                knobColor = Color.Black,
                trackColor = Color.White
            ),
        state = state
    )
    {

        itemsIndexed(Presets.presetList)
        { index, item ->

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .combinedClickable(
                        onClick = {
                            vm.onClickPresetsRead(item)
                        }, onLongClick = {
                            Presets.isOpenDialogDeleteRenameName = item
                            Presets.isOpenDialogDeleteRename.value = true
                        }),


                text = "$index $item", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.neomatrixcode))
            )
        }

    }

}


@Composable
private fun TopBar() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(MaterialColor.GREEN_400),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Пресеты",
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            fontSize = 24.sp
        )

    }
}

@Composable
private fun BottomBar() {
    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(Color.LightGray),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        //Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = {
                Presets.isOpenDialogNewFile.value = true
            }) {
            Icon(painter = painterResource(R.drawable.add2), contentDescription = null)
        }

        OutlinedButton(onClick = { navController.popBackStack() }) {
            Text(text = "Закрыть")
        }

    }
}
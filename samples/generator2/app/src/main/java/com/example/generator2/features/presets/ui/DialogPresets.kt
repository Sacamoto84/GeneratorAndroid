package com.example.generator2.features.presets.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.generator2.features.presets.Presets
import com.example.generator2.features.presets.presetsGetListName
import com.example.generator2.features.presets.presetsVM
import com.siddroid.holi.colors.MaterialColor


val PresetsDialogRecompose = mutableIntStateOf(0)

@Composable
fun DialogPresets(vm: presetsVM = hiltViewModel()) {

    Presets.presetList = presetsGetListName(vm.appPath)

    PresetsDialogRecompose

    Surface(
        modifier = Modifier
            .fillMaxSize()
        //.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
        ,
        shape = RoundedCornerShape(size = 8.dp),
        color = Color(0xFFF8F8F8)
    ) {


        if (Presets.isOpenDialogRename.collectAsState().value) {
            DialogPresetsRename(Presets.isOpenDialogDeleteRenameName)
        }

        if (Presets.isOpenDialogDelete.collectAsState().value) {
            DialogPresetsDelete(Presets.isOpenDialogDeleteRenameName)
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
            .padding(start = 8.dp, end = 8.dp)
//            .scrollbar(
//                count = count,
//                state = state,
//                horizontal = false,
//                hiddenAlpha = 0f,
//                knobColor = Color.Black,
//                trackColor = Color.White
//            )
        ,
        state = state
    )
    {

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }




        itemsIndexed(Presets.presetList)
        { index, item ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Color(0xFFF0F0F0))
                    .border(1.dp, Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))


            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        .offset(x = 0.dp, y = 8.dp)
                        //.border(1.dp, Color.Black)
                        .combinedClickable(
                            onClick = {
                                vm.onClickPresetsRead(item)
                            }, onLongClick = {
                                //Presets.isOpenDialogDeleteRenameName = item
                                //Presets.isOpenDialogDeleteRename.value = true
                            }),
                    text = "$index $item", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.neomatrixcode))
                )


                IconButton(onClick = {

                    Presets.isOpenDialogDeleteRenameName = item
                    Presets.isOpenDialogRename.value = true

                }) {
                    Icon(
                        painter = painterResource(R.drawable.edit), contentDescription = null,
                        modifier = Modifier
                        //.size(36.dp),
                        ,
                        tint = Color.Black
                    )
                }

                IconButton(onClick = {

                    Presets.isOpenDialogDeleteRenameName = item
                    Presets.isOpenDialogDelete.value = true

                }) {
                    Icon(
                        painter = painterResource(R.drawable.delete), contentDescription = null,
                        modifier = Modifier
                        //.size(36.dp),
                        ,
                        tint = Color.Black
                    )
                }

            }


//            Spacer(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(1.dp)
//                    .padding(start = 4.dp)
//                    .background(Color.LightGray)
//            )


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
            .height(48.dp)
            .fillMaxWidth()
            .background(Color.LightGray),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedButton(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Black,
                containerColor = Color.White
            )
        ) {
            Text(text = "Закрыть")
        }

    }
}
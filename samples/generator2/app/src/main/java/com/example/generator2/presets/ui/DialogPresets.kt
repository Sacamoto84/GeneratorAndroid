package com.example.generator2.presets.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.generator2.R
import com.example.generator2.presets.Presets
import com.siddroid.holi.colors.MaterialColor
import libs.modifier.scrollbar


@Composable
fun DialogPresets() {
    val dialogOpen = Presets.isOpenDialog.collectAsState()

    if (dialogOpen.value) {

        Dialog(
            onDismissRequest = { Presets.isOpenDialog.value = false },
            properties = DialogProperties(
                dismissOnClickOutside = false
            )
        )
        {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 16.dp)
                //.wrapContentHeight()
                ,
                shape = RoundedCornerShape(size = 10.dp),
                color = Color.White
            ) {

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
                        Content()
                    }
                }

            }
        }
    }
}


@Composable
private fun Content() {

    val state: LazyListState = rememberLazyListState()

    val count = Presets.presetList.count { it.isNotEmpty() }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            //.simpleHorizontalScrollbar(lazyListState)
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
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                text = "$index $item", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.neomatrixcode))
            )
        }

    }


}


@Composable
private fun TopBar() {
    Column(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(MaterialColor.GREEN_400)
    ) {

    }
}

@Composable
private fun BottomBar() {
    Column(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(Color.LightGray)
    ) {

        OutlinedButton(onClick = { Presets.isOpenDialog.value = false }) {
            Text(text = "Закрыть")
        }



    }
}
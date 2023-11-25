package com.example.generator2.screens.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.generator2.R
import com.example.generator2.navController
import com.example.generator2.screens.editor.dialog.DialogNew
import com.example.generator2.screens.editor.ui.*
import com.example.generator2.theme.colorLightBackground
import com.example.generator2.screens.mainscreen4.VMMain4

private val sizeCanvaChar = 8f
private val strokeWidth = 6f

val openDialogNew = mutableStateOf(false)

@Composable
fun ScreenEditor(global: VMMain4 = hiltViewModel()) {


    Scaffold(bottomBar = { BottomBar(global) }) {

        Column(
            Modifier
                .padding(bottom = it.calculateBottomPadding())
                .fillMaxSize() //  .recomposeHighlighter()
                //.background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {

            if (openDialogNew.value) DialogNew(openDialogNew)

            //Preview
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth() //
                    .border(
                        1.dp, brush = Brush.verticalGradient(
                            listOf(
                                Color.Gray, Color.Gray, Color.DarkGray
                            )
                        ), RectangleShape
                    ) //.clip(RoundedCornerShape(16.dp))
                    .background(colorLightBackground)
            ) {
                EditorPreviewCarrier(model)
                EditorPreviewFM(model)
            }

            Row(
                modifier = Modifier
                    .height(232.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, bottom = 8.dp)
                            .weight(0.5f)
                    ) {
                        ButtonPoint()
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp)
                            .weight(0.5f)
                    ) {
                        ButtonLine()
                    }
                }

                Row(
                    Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .background(Color.Red)
                ) {

                    Column(Modifier.weight(0.5f)) {

                        EditorCanvasLoop()

                        //Горизонтальное усиление
                        HorizontalScaleButton()

                    }

                    //Вертикальные кнопки масштаба
                    VerticalScaleButton()

                }


            }


            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth() //
                    .border(
                        1.dp, brush = Brush.verticalGradient(
                            listOf(
                                Color.Gray, Color.Gray, Color.DarkGray
                            )
                        ), RectangleShape
                    ) //.clip(RoundedCornerShape(16.dp))
                    .background(colorLightBackground)
            ) {
                EditorCanvas()
            }


            //Блок кнопок для линкования
            EditorLinkButtons(Modifier.weight(1f))


        }

    }


}


@Composable
private fun BottomBar(global: VMMain4) {
    BottomAppBar(
        backgroundColor = colorLightBackground,
        contentColor = Color.White,
    ) {


        //Кнопка назад
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(painter = painterResource(R.drawable.back4), contentDescription = null)
        }

        Column() {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.row_triple),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(" " + model.stateEditMax.value.toString())
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.column_triple),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(text = " " + model.stateEditWight.value.toString())
            }

        }

        Spacer(modifier = Modifier.weight(0.1f))

        IconButton(onClick = { openDialogNew.value = true }) {
            Icon(
                painter = painterResource(R.drawable.set_square_geometry),
                contentDescription = null
            )
        }

        IconButton(onClick = { }) {
            Icon(painter = painterResource(R.drawable.folder_open2), contentDescription = null)
        }

        IconButton(onClick = { openDialogNew.value = true }) {
            Icon(painter = painterResource(R.drawable.save2), contentDescription = null)
        }


    }

}

private val colorLine = Color.Gray
private val colorButtonBaground = colorLightBackground
private val colorTextBackround = Color.Gray
private val colorText = Color.Black

//Вертикальные кнопки масштаба
@Composable
fun VerticalScaleButton() {

    Column() {
        Box(
            Modifier
                .height(70.dp)
                .width(30.dp)
                .background(colorButtonBaground)
                .clickable { model.gainYYInc() }, contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier, onDraw = {
                drawLine(
                    start = Offset(
                        size.width / 2 - sizeCanvaChar.dp.toPx(), size.height / 2
                    ), end = Offset(
                        size.width / 2 + sizeCanvaChar.dp.toPx(), size.height / 2
                    ), strokeWidth = strokeWidth, color = colorLine, cap = StrokeCap.Round
                )

                drawLine(
                    start = Offset(
                        size.width / 2, size.height / 2 - sizeCanvaChar.dp.toPx()
                    ), end = Offset(
                        size.width / 2, size.height / 2 + sizeCanvaChar.dp.toPx()
                    ), strokeWidth = strokeWidth, color = colorLine, cap = StrokeCap.Round
                )

            })
        }

        Box(
            Modifier
                .height(60.dp)
                .width(30.dp)
                .background(colorTextBackround),
            contentAlignment = Alignment.Center
        ) {

            if (model.gainYY.value < 1.0f) Text(
                text = model.gainYY.value.toString(), fontSize = 16.sp, color = colorText
            )
            else Text(
                text = model.gainYY.value.toInt().toString(), fontSize = 24.sp, color = colorText
            )

        }

        //+
        Box(
            Modifier
                .height(70.dp)
                .width(30.dp)
                .background(colorButtonBaground)
                .clickable { model.gainYYDec() }, contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier, onDraw = {
                drawLine(
                    start = Offset(
                        size.width / 2 - sizeCanvaChar.dp.toPx(), size.height / 2
                    ), end = Offset(
                        size.width / 2 + sizeCanvaChar.dp.toPx(), size.height / 2
                    ), strokeWidth = strokeWidth, color = colorLine, cap = StrokeCap.Round
                )

            })
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(30.dp)
                .background(Color.LightGray)
                .clickable { model.gainYYNormalize() }, contentAlignment = Alignment.Center
        ) {
            Text(text = "N", fontSize = 18.sp, color = colorText)
        }

    }

}

//Горизонтальное усиление
@Composable
fun HorizontalScaleButton() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.Blue),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .weight(1f)
                .background(colorButtonBaground)
                .clickable { model.gainXXDec() }, contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier, onDraw = {
                drawLine(
                    start = Offset(
                        size.width / 2 - sizeCanvaChar.dp.toPx(), size.height / 2
                    ), end = Offset(
                        size.width / 2 + sizeCanvaChar.dp.toPx(), size.height / 2
                    ), strokeWidth = strokeWidth, color = colorLine, cap = StrokeCap.Round
                )
            })
        }

        Box(
            Modifier
                .fillMaxHeight()
                .width(70.dp)
                .background(colorTextBackround),
            contentAlignment = Alignment.Center
        ) {

            if (model.gainXX.value < 1.0f) Text(
                text = model.gainXX.value.toString(), fontSize = 24.sp, color = colorText
            )
            else Text(
                text = model.gainXX.value.toInt().toString(), fontSize = 24.sp, color = colorText
            )

        }

        // +
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .weight(1f)
                .background(colorButtonBaground)
                .clickable { model.gainXXInc() }, contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier, onDraw = {
                drawLine(
                    start = Offset(
                        size.width / 2 - sizeCanvaChar.dp.toPx(), size.height / 2
                    ), end = Offset(
                        size.width / 2 + sizeCanvaChar.dp.toPx(), size.height / 2
                    ), strokeWidth = strokeWidth, color = colorLine, cap = StrokeCap.Round
                )

                drawLine(
                    start = Offset(
                        size.width / 2, size.height / 2 - sizeCanvaChar.dp.toPx()
                    ), end = Offset(
                        size.width / 2, size.height / 2 + sizeCanvaChar.dp.toPx()
                    ), strokeWidth = strokeWidth, color = colorLine, cap = StrokeCap.Round
                )

            })
        }

    }

}







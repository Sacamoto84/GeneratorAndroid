package com.example.generator2.element

//import androidx.compose.runtime.*
//import androidx.compose.runtime.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import kotlinx.coroutines.delay


class Console2 {

    var SelectVisible = mutableStateOf(true)

    var SelectLine = mutableStateOf(0) //Линия которую нужно подсвечивать

    //MARK: Показывать номер строки
    val isCheckedUselineVisible = mutableStateOf(false)

    //MARK: Размер строки
    val textSizeDefault: TextUnit = 12.sp

    val defaultTextColor: Color = Color.White
    val defaultBgColor: Color = Color.Black

    data class PairTextAndColor(
        var text: String,
        var colorText: Color,
        var colorBg: Color,
        var bold: Boolean = false,
        var italic: Boolean = false,
        var underline: Boolean = false,
        var flash: Boolean = false,
        var textSize: TextUnit = 16.sp
    )

    data class LineTextAndColor(
        var text: String, //Строка вообще
        var pairList: MutableList<PairTextAndColor> //То что будет отрендеренно в этой строке
    )

    var colorlineAndText = mutableStateListOf<LineTextAndColor>()


    fun println(
        text: String,
        color: Color = Color.Green,
        bgColor: Color = Color.Black,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
        flash: Boolean = false,
        textSize: TextUnit = textSizeDefault
    ) {

        if ((colorlineAndText.size == 1) && (colorlineAndText[0].pairList[0].text == "")) {
            colorlineAndText.removeAt(0)
        }

        colorlineAndText.add(

            LineTextAndColor(
                text, listOf(
                    PairTextAndColor(
                        text = text,
                        colorText = color,
                        colorBg = bgColor,
                        bold = bold,
                        italic = italic,
                        underline = underline,
                        flash = flash,
                        textSize = textSize
                    )
                ).toMutableList()
            )
        )
    }


    fun print(
        text: String,
        color: Color = Color.Green,
        bgColor: Color = Color.Black,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
        flash: Boolean = false,
        textSize: TextUnit = textSizeDefault
    ) {

        colorlineAndText.last().pairList.add(
            PairTextAndColor(
                text = text,
                colorText = color,
                colorBg = bgColor,
                bold = bold,
                italic = italic,
                underline = underline,
                flash = flash,
                textSize = textSize
            )
        )

    }


    @Composable //fun Lazy(messages: SnapshotStateList<LineTextAndColor>) {
    fun Draw(modifier: Modifier = Modifier) {

        if (colorlineAndText.isEmpty()) println("")

        //val message: SnapshotStateList<LineTextAndColor> = colorlineAndText

        val messages = colorlineAndText

        var update by remember { mutableStateOf(true) }  //для мигания
        val lazyListState: LazyListState = rememberLazyListState()
        var lastVisibleItemIndex by remember { mutableStateOf(0) }
        lastVisibleItemIndex =
            lazyListState.layoutInfo.visibleItemsInfo.lastIndex + lazyListState.firstVisibleItemIndex

        if (messages.isEmpty()) return

        LaunchedEffect(key1 = messages) {
            while (true) {
                delay(700L)
                update =
                    !update
            }
        }

        LaunchedEffect(key1 = lastVisibleItemIndex, key2 = messages) {
            while (true) {
                delay(200L) //val s = messages.size
                if (messages.size > 5) {
                    lazyListState.scrollToItem(index = messages.size - 1) //Анимация (плавная прокрутка) к данному элементу.
                }
            }
        } //.background(Color(0xFF090909))


        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF090909))
                .then(modifier) //.weight(1f)
        ) {

            ///////////////////////////////////////////////////////////
            LazyColumn(                                              //
                modifier = Modifier.fillMaxSize(),
                state = lazyListState                          //
            ) {

                //val z = manual_recomposeLazy.value
                //println("LAZY Счетчик рекомпозиций $z")


                itemsIndexed(messages.toList()) { index, item ->
                    Row() {

                        val s = item.pairList.size

                        if ((s > 0) && (isCheckedUselineVisible.value)) {

                            val str: String = when (index) {
                                in 0..9 -> String.format("   %d>", index)
                                in 10..99 -> String.format("  %d>", index)
                                in 100..999 -> String.format(" %d>", index)
                                else -> String.format("%d>", index)
                            }
                            Text(
                                text = str,
                                color = if (item.text.isBlank()) Color.Black else Color.Gray,
                                fontSize = textSizeDefault,
                                fontFamily = FontFamily(
                                    Font(
                                        com.example.generator2.R.font.jetbrains,
                                        FontWeight.Normal
                                    )
                                )
                            )
                        }


                        for (i in 0 until s) {

                            //val colorSelect = if (SelectLine.value == index) Color.Magenta else Color.DarkGray

                            Box(
                                Modifier.fillMaxWidth() //.background(colorSelect)
                            ) {

                                Text(
                                    text = item.pairList[i].text,
                                    color = if (!item.pairList[i].flash) item.pairList[i].colorText
                                    else if (update) item.pairList[i].colorText else Color(
                                        0xFF090909
                                    ),
                                    modifier = Modifier.background(
                                        if (!item.pairList[i].flash) item.pairList[i].colorBg
                                        else if (update) item.pairList[i].colorBg else Color(
                                            0xFF090909
                                        )
                                    ),
                                    textDecoration = if (item.pairList[i].underline) TextDecoration.Underline else null,
                                    fontWeight = if (item.pairList[i].bold) FontWeight.Bold else null,
                                    fontStyle = if (item.pairList[i].italic) FontStyle.Italic else null,
                                    fontSize = item.pairList[i].textSize,
                                    fontFamily = FontFamily(
                                        Font(R.font.jetbrains, FontWeight.Normal)
                                    )
                                )
                            }

                        }


                    }
                }


            }


        }
    }


}




















package com.example.generator2.console

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import kotlinx.coroutines.delay

//var manual_recomposeLazy = mutableStateOf(0)

//println("Индекс первого видимого элемента = " + lazyListState.firstVisibleItemIndex.toString())
//println("Смещение прокрутки первого видимого элемента = " + lazyListState.firstVisibleItemScrollOffset.toString())
//println("Количество строк выведенных на экран lastIndex = " + lazyListState.layoutInfo.visibleItemsInfo.lastIndex.toString())

class Console(driver: Int = 0) {

    val messages = mutableListOf<LineTextAndColor>()

    private var recompose by mutableStateOf(0)
    private var driver by mutableStateOf(driver)

    var lineVisible by mutableStateOf(false)

    var tracking by mutableStateOf(false) //Слежение за последним полем
    var lastCount by mutableStateOf(0)


    //Настройка шрифтов
    val fontSize by mutableStateOf(12.sp)                                     //Размер шрифта
    val fontFamily = FontFamily(Font(R.font.jetbrains, FontWeight.Normal))


    //Рекомпозиция списка
    fun recompose() {
        recompose++
    }

    @Composable
    fun lazy(modifier: Modifier = Modifier) {
        var update by remember { mutableStateOf(true) }  //для мигания
        val lazyListState: LazyListState = rememberLazyListState()

        var lastVisibleItemIndex by remember { mutableStateOf(0) }

        lastVisibleItemIndex =
            lazyListState.layoutInfo.visibleItemsInfo.lastIndex + lazyListState.firstVisibleItemIndex
        //println("Последний видимый индекс = $lastVisibleItemIndex")


        LaunchedEffect(key1 = messages) {
            while (true) {
                delay(700L)
                update = !update
                //////////////////////telnetWarning.value = (telnetSlegenie.value == false) && (messages.size > lastCount)
            }
        }

        LaunchedEffect(key1 = lastVisibleItemIndex) {
            while (true) {
                delay(200L)
                val s = messages.size
                if ((s > 20) && tracking) {
                    lazyListState.scrollToItem(index = messages.size - 1) //Анимация (плавная прокрутка) к данному элементу.
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF090909))
                .then(modifier)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f))
            {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                       , state = lazyListState)
                {

                    recompose

                    itemsIndexed(messages.toList())
                    { index, item ->

                        if (driver == 1)
                        ////////////////////ScriptItemDraw({ item }, { index }, { false })
                        else

                            Row()
                            {

                                val s = item.pairList.size
                                if ((s > 0) && (lineVisible)) {
                                    val str: String = when (index) {
                                        in 0..9 -> String.format("   %d>", index)
                                        in 10..99 -> String.format("  %d>", index)
                                        in 100..999 -> String.format(" %d>", index)
                                        else -> String.format("%d>", index)
                                    }
                                    Text(
                                        text = str,
                                        color = if (item.pairList.isEmpty()) Color.DarkGray else Color.Gray,
                                        fontSize = fontSize,
                                        fontFamily = fontFamily
                                    )
                                }

                                for (i in 0 until s) {
                                    Text(
                                        text = item.pairList[i].text,
                                        color = if (!item.pairList[i].flash)
                                            item.pairList[i].colorText
                                        else
                                            if (update) item.pairList[i].colorText else Color(
                                                0xFF090909
                                            ),
                                        modifier = Modifier.background(
                                            if (!item.pairList[i].flash) item.pairList[i].colorBg else if (update) item.pairList[i].colorBg else Color(
                                                0xFF090909
                                            )
                                        ),
                                        textDecoration = if (item.pairList[i].underline) TextDecoration.Underline else null,
                                        fontWeight = if (item.pairList[i].bold) FontWeight.Bold else null,
                                        fontStyle = if (item.pairList[i].italic) FontStyle.Italic else null,
                                        fontSize = fontSize,
                                        fontFamily = fontFamily
                                    )

                                }

                            }
                    }
                }

            }

        }
    }

    fun consoleAdd(text: String, color: Color = Color.Green, bgColor: Color = Color.Black) {
        if ((messages.size > 0) && (messages.last().text == " ")) {
            messages.removeAt(messages.lastIndex)
            messages.add(
                LineTextAndColor(
                    text,
                    listOf(PairTextAndColor(text = text, color, bgColor))
                )
            )
        } else {
            messages.add(
                LineTextAndColor(
                    text,
                    listOf(PairTextAndColor(text = text, color, bgColor))
                )
            )
        }
    }

}











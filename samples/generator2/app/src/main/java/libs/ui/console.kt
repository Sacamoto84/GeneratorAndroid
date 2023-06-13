package libs.ui

//import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

class Console1 {

    var currentTextColor: Color = Color.Green
    var currentBgColor: Color = Color.Black
    private val colorLine = mutableStateListOf<List<PairTextAndColor>>()

    private data class PairTextAndColor(
        var text: String,
        var colorText: Color,
        var colorBg: Color,
        var bold: Boolean = false,
        var italic: Boolean = false,
        var underline: Boolean = false,
    )

    @Composable
    fun Draw() {
        val lazyListState: LazyListState = rememberLazyListState()

        var lastVisibleItemIndex by remember { mutableStateOf(0) }
        lastVisibleItemIndex =
            lazyListState.layoutInfo.visibleItemsInfo.lastIndex + lazyListState.firstVisibleItemIndex

        LaunchedEffect(key1 = true, key2 = lastVisibleItemIndex) {
            while (true) {
                delay(200L)
                val s = colorLine.size
                if (s > 0) {
                    lazyListState.scrollToItem(index = s - 1)
                }
            }
        }

        Column(
            Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(), state = lazyListState
            ) {
                itemsIndexed(colorLine)
                { index, l ->
                    Row()
                    {

//                        var s = l.size
//                        if (s > 0) {
//                            val str: String = when (index) {
//                                in 0..9 -> String.format("   %d>", index)
//                                in 10..99 -> String.format("  %d>", index)
//                                in 100..999 -> String.format(" %d>", index)
//                                else -> String.format("%d>", index)
//                            }
//                            Text(
//                                text = str,
//                                color = Color.Gray,
//                                fontFamily = FontFamily.Monospace
//                            )
//                        }
//                        for (i in 0 until s) {
//                            Text(
//                                text = l[i].text,
//                                color = currentTextColor,
//                                modifier = Modifier.background(currentBgColor),
//                                //textDecoration = if (l[i].underline) TextDecoration.Underline else null,
//                                //fontWeight = if (l[i].bold) FontWeight.Bold else null,
//                                //fontStyle = if (l[i].italic) FontStyle.Italic else null,
//                                fontFamily = FontFamily.Monospace
//                            )
//                        }


                    }
                }

            }
        }
    }

    fun add(value: String) {
        colorLine.add(
            listOf(
                PairTextAndColor(
                    text = value, Color.Green, Color.Black
                )
            )
        )
    }


}












package com.example.generator2.screens.scripting.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.R
import com.example.generator2.theme.colorDarkBackground

/**
 * Список форм сигнала с картинками — не сетка клавиш, а прокручиваемый выбор.
 *
 * @param type из какой библиотеки брать формы: CR несущая, AM или FM.
 */
@Composable
internal fun KeyboardScreenModulation(keyboard: ScriptKeyboard, arg: Int, type: String = "CR") {

    val lazyListState: LazyListState = rememberLazyListState()
    val selectedIndex = remember { mutableStateOf(0) }

    val forms = when (type) {
        "CR" -> keyboard.gen.itemlistCarrier.toList()
        "AM" -> keyboard.gen.itemlistAM.toList()
        else -> keyboard.gen.itemlistFM.toList()
    }

    Row {
        LazyColumn(
            modifier = Modifier
                .height(192.dp)
                .fillMaxWidth()
                .weight(1f)
                .background(colorDarkBackground),
            state = lazyListState
        ) {
            itemsIndexed(forms) { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.2.dp, Color.Magenta)
                        .selectable(selected = selectedIndex.value == index, onClick = {
                            keyboard.put(arg, item.name)
                            keyboard.goHome()
                        })
                ) {
                    item.bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 6.dp, top = 2.dp, bottom = 2.dp, end = 20.dp)
                                .height(40.dp)
                        )
                    }
                    Text(
                        text = item.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.jetbrains))
                    )
                }
            }
        }
        Box(Modifier.width(64.dp)) {
            KeyBack(keyboard)
        }
    }
}

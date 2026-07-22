package com.example.generator2.common.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.generator2.R
import com.example.generator2.common.eventbus.Event
import com.example.generator2.common.eventbus.EventBus

/**
 * Короткий способ показать сообщение из любого места приложения:
 * ```kotlin
 * SnackBar.success("Пресет применён")
 * ```
 * Сообщение уходит в [EventBus], показывает его корневой экран.
 */
object SnackBar {

    fun info(message: String) {
        EventBus.postEvent(Event.ShowSnackBar(UiMessage.Info(message)))
    }

    fun error(message: String) {
        EventBus.postEvent(Event.ShowSnackBar(UiMessage.Error(message)))
    }

    fun success(message: String) {
        EventBus.postEvent(Event.ShowSnackBar(UiMessage.Success(message)))
    }

    fun warning(message: String) {
        EventBus.postEvent(Event.ShowSnackBar(UiMessage.Warning(message)))
    }
}

/**
 * Отрисовка одного снекбара. Отделена от хоста, чтобы её можно было смотреть в превью.
 */
@Composable
fun RenderSnackBarFilled(uiMsg: UiMessage) {

    //Цвет только у полоски и иконки, фон всегда белый
    val (accent, icon) = when (uiMsg) {
        is UiMessage.Success -> Color(0xFF01B671) to Icons.Rounded.Check
        is UiMessage.Error -> Color(0xFFE5553A) to Icons.Rounded.ErrorOutline
        is UiMessage.Info -> Color(0xFF4276FE) to Icons.Rounded.Info
        is UiMessage.Warning -> Color(0xFFFF8E0C) to Icons.Rounded.WarningAmber
    }

    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        color = Color.White,
        contentColor = Color.Black,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            Modifier
                .height(IntrinsicSize.Min)
                .zIndex(Float.MAX_VALUE),
            verticalAlignment = Alignment.CenterVertically
        ) {

            //Вертикальная полоска цвета сообщения
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            Spacer(Modifier.width(10.dp))

            Icon(
                icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(8.dp))

            //В оригинале Poppins Medium, у нас из гротесков есть nunito
            Text(
                uiMsg.text,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, end = 14.dp),
                color = Color.Black,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.nunito))
            )
        }
    }
}

@Preview
@Composable
private fun RenderSnackBarSuccessPreview() {
    RenderSnackBarFilled(UiMessage.Success("Пресет «Bell» применён"))
}

@Preview
@Composable
private fun RenderSnackBarErrorPreview() {
    RenderSnackBarFilled(UiMessage.Error("Не удалось применить пресет"))
}

@Preview
@Composable
private fun RenderSnackBarInfoPreview() {
    RenderSnackBarFilled(UiMessage.Info("Скрипт окончен"))
}

@Preview
@Composable
private fun RenderSnackBarWarningPreview() {
    RenderSnackBarFilled(UiMessage.Warning("Частота ниже 50 Гц"))
}

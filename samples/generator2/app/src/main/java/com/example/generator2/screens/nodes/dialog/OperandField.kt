package com.example.generator2.screens.nodes.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.script.Operand
import com.example.generator2.features.script.REGISTER_COUNT

private val ConstColor = Color(0xFF3A7BD5)
private val RegColor = Color(0xFFA06CD5)

/**
 * Значение параметра: число либо регистр.
 *
 * Чип слева переключает режим. Так видно с одного взгляда, что в поле,
 * и в режиме числа можно показать цифровую клавиатуру — с одним общим
 * текстовым полем пришлось бы держать буквенную ради «F1».
 */
@Composable
fun OperandField(
    value: Operand,
    onChange: (Operand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReg = value is Operand.Reg

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {

        Text(
            text = if (isReg) "F" else "123",
            color = if (isReg) Color.White else ConstColor,
            fontSize = 11.sp,
            modifier = Modifier
                .background(
                    if (isReg) RegColor else Color.Transparent,
                    RoundedCornerShape(5.dp),
                )
                .border(1.dp, if (isReg) RegColor else ConstColor, RoundedCornerShape(5.dp))
                .clickable {
                    //Значение при переключении не переносим: число в номер
                    //регистра не превратить осмысленно, и наоборот
                    onChange(if (isReg) Operand.Const(0f) else Operand.Reg(0))
                }
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )

        Box(Modifier.width(8.dp))

        if (isReg) {
            RegisterPicker((value as Operand.Reg).index) { onChange(Operand.Reg(it)) }
        } else {
            ConstInput((value as Operand.Const).value) { onChange(Operand.Const(it)) }
        }
    }
}

@Composable
private fun ConstInput(value: Float, onChange: (Float) -> Unit) {
    //TextFieldValue, а не String: со строкой Compose пересоздавал бы поле из
    //value на каждый ввод ("1" -> onChange -> "1.0"), и курсор прыгал бы в
    //конец. TextFieldValue хранит и текст, и позицию курсора, поэтому ввод
    //ведёт себя как обычно. Ключа remember нет: source of truth — само поле,
    //а при смене чипа 123/F весь ConstInput пересоздаётся заново.
    var tfv by remember { mutableStateOf(TextFieldValue(value.toString())) }

    BasicTextField(
        value = tfv,
        onValueChange = {
            tfv = it
            it.text.toFloatOrNull()?.let(onChange)
        },
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, textAlign = TextAlign.End),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(84.dp)
            .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@Composable
private fun RegisterPicker(index: Int, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Box {
        Text(
            "F$index ▾",
            color = RegColor,
            fontSize = 13.sp,
            modifier = Modifier
                .width(84.dp)
                .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
                .clickable { open = true }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            textAlign = TextAlign.End,
        )

        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (0 until REGISTER_COUNT).forEach { i ->
                DropdownMenuItem(
                    text = { Text("F$i") },
                    onClick = {
                        onPick(i)
                        open = false
                    },
                )
            }
        }
    }
}

/** Выпадающий список известных генератору форм сигнала */
@Composable
fun WaveformPicker(value: String, names: List<String>, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Box {
        Text(
            if (value.isEmpty()) "выбрать ▾" else "$value ▾",
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier
                .width(120.dp)
                .background(Color(0xFF3D3D3F), RoundedCornerShape(5.dp))
                .clickable { open = true }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            textAlign = TextAlign.End,
        )

        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            names.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onPick(name)
                        open = false
                    },
                )
            }
        }
    }
}

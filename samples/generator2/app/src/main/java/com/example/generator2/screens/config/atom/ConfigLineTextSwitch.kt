package com.example.generator2.screens.config.atom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.generator2.screens.config.DefScreenConfig
//import libs.modifier.scrollbar

@Composable
fun ConfigLineTextSwitch(text: String = "", checked: Boolean, action: ((Boolean) -> Unit)?) {

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = text,
            color = DefScreenConfig.textColor,
            maxLines = 3,
            minLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = DefScreenConfig.textStyle
        )

        Switch(
            modifier = Modifier
                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                .width(DefScreenConfig.widthEdit)
                .height(DefScreenConfig.heightEdit),

            checked = checked, onCheckedChange = action,
            thumbContent = {

                if (checked)
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "",
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                else
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "",
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )


            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = DefScreenConfig.backgroundColorGreenButton,
                checkedIconColor =  DefScreenConfig.backgroundColorGreenButton,

                uncheckedTrackColor = Color(0xFF49454F),
                uncheckedBorderColor = Color(0xFF938F99),
                uncheckedThumbColor = Color(0xFF938F99),
                uncheckedIconColor = Color(0xFF49454F),


            )


        )


//        Text(
//            text2,
//            Modifier
//                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
//                .width(DefScreenConfig.widthEdit)
//                .height(DefScreenConfig.heightEdit)
//            , style = TextStyle(
//                fontSize = 20.sp,
//                fontWeight = FontWeight.W600,
//                color = Color.White,
//                textAlign = TextAlign.Center,
//                baselineShift = BaselineShift(-0.1f)
//            )
//        )

    }

}
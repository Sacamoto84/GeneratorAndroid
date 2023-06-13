package com.example.generator2.screens.mainscreen4.card

import CardCarrier
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.generator2.model.LiveData
import com.example.generator2.screens.mainscreen4.impulse.CardImpulse
import com.example.generator2.theme.colorLightBackground

@Composable
fun CardCard(str: String = "CH0") {


    val ch: State<Boolean> = if (str == "CH0") {
        LiveData.impulse0.collectAsState()
    } else {
        LiveData.impulse1.collectAsState()
    }

    Card(
        backgroundColor = colorLightBackground,
        modifier = Modifier
            .height(258.dp)
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp)
    )
    {

        AnimatedContent(
            targetState = ch.value,
            transitionSpec = {
                val time = 400
                //Появление
                (fadeIn(animationSpec = tween(time / 2)) + expandVertically(
                    animationSpec = tween(
                        time
                    )
                ))
                    .togetherWith(
                        (fadeOut(animationSpec = tween(time)) + shrinkVertically(
                            animationSpec = tween(time)
                        ))
                    ).using(
                        SizeTransform(
                            clip = true,
                            sizeAnimationSpec = { _, _ -> tween(time) })
                    )
            }, label = ""
        )
        {
            if (it)
                CardImpulse(str)
            else
                CardCarrier(str)
        }


    }

}







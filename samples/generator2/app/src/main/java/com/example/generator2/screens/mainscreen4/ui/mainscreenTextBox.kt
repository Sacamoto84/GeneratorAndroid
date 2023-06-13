package com.example.generator2.screens.mainscreen4.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import libs.modifier.recomposeHighlighterOneLine
import timber.log.Timber


enum class DragStatus {
    IDLE,
    DRAG,
}


@Composable
fun MainScreenTextBoxGuest(
    str: String,
    modifier: Modifier = Modifier,
    sensing: Float = 1.0f, //Чувствительность
    value: Float, //Вывод icrementalAngle от 0 до rangeAngle при rangeAngle != 0, и от +- Float при rangeAngle = 0
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float> = 0f..1000f,
    useDrag : Boolean = false,
    fontSize: TextUnit = 24.sp,
    fontFamily: FontFamily = FontFamily.Default
    ) {

    val onValueChangeState = rememberUpdatedState(onValueChange)
    var incrementalAngle by remember { mutableFloatStateOf(value) }
    incrementalAngle = value //valueRemember.value

    var status by remember {
        mutableStateOf(DragStatus.IDLE)
    }

    var contentAlignment by remember { mutableStateOf(Alignment.Center) }

//    val animatedContent = @Composable { alignment: Alignment ->
//
//        AnimatedContent(
//            targetState = alignment, label = "",
//
//            transitionSpec = {
//                (expandHorizontally(animationSpec = tween(10000)){-it/10})
//                    .togetherWith(shrinkHorizontally(animationSpec = tween(10000)){-it/10}
//                    )
//            },
//
//            )
//        { targetAlignment ->
//
//            Text(
//                text = str,
//                textAlign = if (targetAlignment == Alignment.Center) TextAlign.Center else TextAlign.Start,
//                modifier = Modifier.fillMaxWidth(),
//                color = Color.LightGray,
//                fontSize = fontSize,
//                fontWeight = FontWeight.Bold
//            )
//
//        }
//    }

    Box(
        Modifier
            .then(modifier)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(Color(0xFF13161B))
            .recomposeHighlighterOneLine()
            .pointerInput(Unit) {

                detectDragGestures(

                    onDrag =
                    { change, dragAmount ->

                        Timber.w("dragAmount= $dragAmount")

                        change.consume()

                        incrementalAngle -= dragAmount.y * sensing - dragAmount.x * sensing

                        if (incrementalAngle > range.endInclusive)
                            incrementalAngle = range.endInclusive

                        if (incrementalAngle < range.start)
                            incrementalAngle = range.start

                        onValueChangeState.value.invoke(incrementalAngle)

                        status = DragStatus.DRAG

                    },

                    onDragStart = {
                        status = DragStatus.DRAG
                    },

                    onDragCancel = {
                        status = DragStatus.IDLE
                    },

                    onDragEnd = {
                        status = DragStatus.IDLE
                    }
                )
            },
        contentAlignment = Alignment.CenterStart //contentAlignment //if (status == dragStatus.IDLE) Alignment.Center else Alignment.CenterStart
    ) {

        contentAlignment =
            if ((status == DragStatus.IDLE) or (!useDrag)) Alignment.Center else Alignment.CenterStart

        Text(
            text = str,
            textAlign = if (contentAlignment == Alignment.Center) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold
        )


    }
}


@Composable
fun MainscreenTextBox(str: String, modifier: Modifier = Modifier) {
    Box(
        Modifier
            //.height(48.dp)
            //.fillMaxWidth()
            .then(modifier)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(Color(0xFF13161B))
            .recomposeHighlighterOneLine(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = str,
            color = Color.LightGray,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun MainscreenTextBoxPlus2Line(
    str: String,
    strplus: String,
    strminus: String,
    modifier: Modifier = Modifier
) {
    Box(
        Modifier
            //.height(48.dp)
            //.fillMaxWidth()
            .then(modifier)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(Color(0xFF13161B))
            .recomposeHighlighterOneLine(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            Modifier
                .matchParentSize(),
            //.background(Color.DarkGray),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Text(
                text = str,
                color = Color.LightGray,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 0.dp)
            )


            Column {

                Text(
                    text = strplus,
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = 6.dp)
                )

                Text(
                    text = strminus,
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = 6.dp)
                )
            }

        }

    }
}
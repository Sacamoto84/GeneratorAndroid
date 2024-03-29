package com.example.generator2.screens.mainscreen4

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.generator2.mp3.compose.MP3Control
import com.example.generator2.presets.Presets
import com.example.generator2.presets.ui.DialogPresetsNewFile
import com.example.generator2.screens.mainscreen4.bottom.M4BottomAppBarComponent
import com.example.generator2.screens.mainscreen4.card.CardCard
import com.example.generator2.screens.mainscreen4.top.TopBarAudioSource
import com.example.generator2.theme.colorDarkBackground
import com.example.generator2.update.ui.WidgetUpdate
import timber.log.Timber

@androidx.media3.common.util.UnstableApi
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Mainsreen4(
    vm: VMMain4 = hiltViewModel()
) {

    //val vm = hiltViewModel<VMMain4>()

    Timber.e("mainsreen4")

    if (Presets.isOpenDialogNewFile.collectAsState().value) {
        DialogPresetsNewFile(vm.gen)
    }

    Scaffold(

        topBar = {
            Column {
                WidgetUpdate()
                Text(
                    text = vm.gen.liveData.presetsName.collectAsState().value,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        bottomBar = {
            //Нижняя панель
            M4BottomAppBarComponent(vm)
        }
    )
    { it ->

        val mono by vm.gen.liveData.mono.collectAsState()

        val animateHeight by animateDpAsState(
            targetValue = if (!mono) 314.dp else 0.dp,
            animationSpec = tween(durationMillis = 7050), label = ""
        )

        val animateAlpha by animateFloatAsState(
            targetValue = if (!mono) 1f else 0.0f,
            animationSpec = tween(durationMillis = 7050), label = ""
        )

        //Основной экран
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = it.calculateBottomPadding())
                .background(colorDarkBackground)
                .verticalScroll(rememberScrollState()),
            //verticalArrangement = Arrangement.SpaceEvenly
        ) {

            //Заполнение сверху
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth()
//                    .weight(1f)
//            )

            //Выбор Аудио Источников MP3 Gen Oscill
            TopBarAudioSource(vm)

            AnimatedVisibility(
                visible = vm.scope.isUse.collectAsState().value,
                enter =
                //slideInVertically() +
                expandVertically(expandFrom = Alignment.Bottom),// + fadeIn(),
                exit = //slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })// +
                shrinkVertically(), //+ fadeOut()
            )
            {
                //Осциллограф
                vm.scope.Oscilloscope()
            }

            MP3Control(vm)

            //Mp3Library(vm.exoplayer)

            Column()
            {
                //CardCarrier("CH0")
                CardCard("CH0", vm.gen)
                Spacer(modifier = Modifier.height(8.dp))

                //CardCommander(vm)                                                     //<-- Commander

                //Spacer(modifier = Modifier.height(8.dp))
            }

            AnimatedContent(
                targetState = mono,
                transitionSpec = {
                    val time = 400
                    //Появление
                    (fadeIn(animationSpec = tween(time / 2)) + expandVertically(
                        animationSpec = tween(time)
                    ))
                        .togetherWith(
                            (fadeOut(animationSpec = tween(time)) + shrinkVertically(
                                animationSpec = tween(time)
                            ))
                        ).using(
                            SizeTransform(clip = true, sizeAnimationSpec = { _, _ -> tween(time) })
                        )
                }, label = ""
            )
            {
                if (!it)
                //CardCarrier("CH1")
                    CardCard("CH1", vm.gen)
                else Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .weight(1f)
            )

        }


    }
}

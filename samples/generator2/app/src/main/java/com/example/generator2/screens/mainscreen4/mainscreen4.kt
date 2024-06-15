package com.example.generator2.screens.mainscreen4

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.generator2.AppScreen
import com.example.generator2.features.mp3.compose.MP3Control
import com.example.generator2.features.presets.Presets
import com.example.generator2.features.presets.ui.DialogPresetsNewFile
import com.example.generator2.features.update.ui.WidgetUpdate
import com.example.generator2.screens.config.ScreenConfig
import com.example.generator2.screens.mainscreen4.bottom.M4BottomAppBarComponent
import com.example.generator2.screens.mainscreen4.card.CardCard
import com.example.generator2.screens.mainscreen4.top.TopBarAudioSource
import com.example.generator2.theme.colorDarkBackground
import timber.log.Timber


@Composable
fun Mainsreen4(vm: VMMain4) {

    //val vm: VMMain4 = hiltViewModel()

    val navigator = LocalNavigator.currentOrThrow

    //val vm = hiltViewModel<VMMain4>()

    Timber.e("mainsreen4")

    if (Presets.isOpenDialogNewFile.collectAsState().value) {
        DialogPresetsNewFile(vm.audioMixerPump.gen)
    }

    Scaffold(

        topBar = {
            Column {
                WidgetUpdate(vm.update)
                Text(
                    text = vm.audioMixerPump.gen.liveData.presetsName.collectAsState().value,
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
            M4BottomAppBarComponent(vm, navigateToConfig = { navigator.push(AppScreen.Config) })
        }
    )
    { it ->

        val mono by vm.audioMixerPump.gen.liveData.mono.collectAsState()

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

//            AnimatedVisibility(
//                visible = vm.audioMixerPump.scope.isUse.collectAsState().value,
//                enter =
//                //slideInVertically() +
//                expandVertically(expandFrom = Alignment.Bottom),// + fadeIn(),
//                exit = //slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })// +
//                shrinkVertically(), //+ fadeOut()
//            )
//            {
            //Осциллограф

        //    vm.audioMixerPump.scope.Oscilloscope()

            //}


            MP3Control(vm)




            Column()
            {
                //CardCarrier("CH0")
                CardCard("CH0", vm.audioMixerPump.gen)
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
                            SizeTransform(
                                clip = true,
                                sizeAnimationSpec = { _, _ -> tween(time) })
                        )
                }, label = ""
            )
            {
                if (!it)
                //CardCarrier("CH1")
                    CardCard("CH1", vm.audioMixerPump.gen)
                else Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                )
            }


//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth()
//                    .weight(1f)
//            )


        }


    }
}



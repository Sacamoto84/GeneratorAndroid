package com.example.generator2.update.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.generator2.update.UPDATESTATE
import com.example.generator2.update.Update

@Composable
fun WigetUpdate() {
    val state = Update.state.collectAsState().value //Получить текущее состояние
    val p = Update.percent.collectAsState()

    if (state == UPDATESTATE.DOWNLOADING)

        Column {

            Text(
                text = "Обновление ${Update.currentVersion} -> ${Update.externalVersion}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Row(
                modifier = Modifier
                    //.height(64.dp)
                    .fillMaxWidth()
                    .background(Color.Cyan)
            ) {


                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            //start = 16.dp,
                            //end = 16.dp,
                            //top = 16.dp
                        ),
                    progress = p.value
                )


            }
        }
}
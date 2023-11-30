package com.example.generator2.update.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.generator2.strings.MainResStrings
import com.example.generator2.update.UPDATESTATE
import com.example.generator2.update.Update

@Composable
fun WidgetUpdate() {
    val state = Update.state.collectAsState().value //Получить текущее состояние
    val p = Update.percent.collectAsState()

    if (state == UPDATESTATE.DOWNLOADING)

        Column {

            Text(
                text = MainResStrings.downloading +" ${Update.currentVersion} -> ${Update.externalVersion}",
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
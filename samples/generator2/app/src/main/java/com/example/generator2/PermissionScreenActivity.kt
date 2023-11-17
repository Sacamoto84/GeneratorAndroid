package com.example.generator2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.example.generator2.theme.Generator2Theme
import kotlinx.coroutines.delay


class PermissionScreenActivity : ComponentActivity() {

    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("Запуск PermissionScreenActivity")

        setContent {

            Generator2Theme {

                var granded by remember {
                    mutableStateOf(false)
                }

                if (!PermissionStorage.hasPermissions(this)) {

                    LaunchedEffect(key1 = true, block = {
                        while (!granded) {
                            delay(100)
                            granded = PermissionStorage.hasPermissions(applicationContext)
                        }

                        val intent = Intent(this@PermissionScreenActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    })

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        Arrangement.Center
                    )
                    {
                        Text(
                            text = "Отсуствуют Файловые разрешения",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            color = Color(0xFFFFE800)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { PermissionStorage.requestPermissions(applicationContext) }) {
                            Text(
                                text = "Запрос",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp
                            )
                        }
                    }

                }

            }
        }


    }


}
package com.example.generator2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.alexstyl.warden.PermissionState
import com.alexstyl.warden.Warden
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/*

   PermissionStorage.hasPermissions(context) - проверка что есть пермишен</strong>

   var granded by remember { mutableStateOf(false) }

   if (!PermissionStorage.hasPermissions(this))
   {

     LaunchedEffect(key1 = true, block = {
       while (!granded) {
         delay(100)
         granded = PermissionStorage.hasPermissions(applicationContext)
       }
     })

     Column( modifier = Modifier.fillMaxSize().background(if (!granded) Color.Magenta else Color.Magenta), Arrangement.Center )
     {
       Text( text = "Отсуствуют Файловые разрешения", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center )
       Button( onClick = { PermissionStorage.requestPermissions(applicationContext) }) {
         Text( text = "Запрос", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center )
       }
      }
    }
    else
    {
    }
 */

/**
 * Пример использования цикла for:
 *
 * ```kotlin
 * yourPrefsInstance.editAndCommit {
 *     putString("key", value)
 * }
 * ```
 *
 * | Left-Aligned  | Center Aligned  | Right Aligned |
 * |:------------- |:---------------:| -------------:|
 * | Row 1         | Cell 2          | Cell 3        |

 *
 * | Left-Aligned  | Center Aligned  | Right Aligned |
 * |:------------- |:---------------:| -------------:|
 * | Row 1         | **Bold**        | Cell 3        |
 * | Row 2         | *Italic*        | Cell 6        |

 *
 */
fun isLoggedIn(): Boolean {
    // реализация метода
    return true
}

object PermissionStorage {

    fun hasPermissions(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager() //Проверка есть ли разрешение? >=A11
        } else {
            (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun requestPermissions(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            addFlags(FLAG_ACTIVITY_NEW_TASK)

                        }
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", context.packageName))
                    //activity.startActivityForResult(intent, requestCode);
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    //activity.startActivityForResult(intent, requestCode);
                    context.startActivity(intent)
                }
            }


        } else {

            GlobalScope.launch(Dispatchers.Main) {
                val result = Warden.with(context)
                    .requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                when (result) {
                    is PermissionState.Denied -> Toast.makeText(
                        context,
                        "WRITE_EXTERNAL_STORAGE Denied",
                        Toast.LENGTH_LONG
                    ).show()

                    PermissionState.Granted -> Toast.makeText(
                        context,
                        "WRITE_EXTERNAL_STORAGE Granted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

    }

}
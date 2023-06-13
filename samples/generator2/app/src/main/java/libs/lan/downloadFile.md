```kotlin
package libs.lan

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

fun downloadFile(url: String, file: File) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()

    response.body.let { responseBody ->
        FileOutputStream(file).use { outputStream ->
            responseBody.byteStream().use { inputStream ->
                val buffer = ByteArray(4 * 1024)
                var bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesRead = inputStream.read(buffer)
                }
                outputStream.flush()
            }
        }
    }
}
```

Пример использования функции downloadFile

```kotlin
val url = "https://www.example.com/example.zip"
val file = File("example.zip")
downloadFile(url, file)
```

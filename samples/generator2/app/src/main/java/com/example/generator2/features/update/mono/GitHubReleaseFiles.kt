package com.example.generator2.features.update.mono

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber

//GitHubReleaseFiles("my-username", "my-repo", "v1.0.0").execute().get()?.let { files ->
//    // Обработайте список файлов здесь
//}


/**
 * Получение списка списка файлов по нужному телегу
 */
fun gitHubReleaseFiles(owner: String, repo: String, releaseTag: String): List<String> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.github.com/repos/$owner/$repo/releases/tags/$releaseTag")
        .build()

    val files = mutableListOf<String>()

    try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return files
        }
        val responseBody = response.body.string()
        val json = JSONObject(responseBody)
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            files.add(asset.getString("name"))
        }
    } catch (e: Exception) {
        Timber.e(e.message)
    }
    return files
}
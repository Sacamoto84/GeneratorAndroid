package com.example.generator2.features.update

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

//GitHubReleaseTags("my-username", "my-repo").execute().get()?.let { tags ->
//    // Обработайте список тегов здесь
//}

/**
 * Получение списка тегов по нужному репозиторию
 */

fun gitHubReleaseTags(owner: String, repo: String): List<String> {

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.github.com/repos/$owner/$repo/tags")
        .build()

    val tags = mutableListOf<String>()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) {
        return tags
    }
    val responseBody = response.body.string()
    val jsonArray = JSONArray(responseBody)
    for (i in 0 until jsonArray.length()) {
        val tag = jsonArray.getJSONObject(i)
        tags.add(tag.getString("name"))
    }

    return tags
}
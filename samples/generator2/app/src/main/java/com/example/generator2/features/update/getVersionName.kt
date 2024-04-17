package com.example.generator2.features.update

import android.content.Context
import android.content.pm.PackageManager

/**
 * Получение версии versionName приложения из гредла
 */
fun getVersionName(context: Context): String {
    try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return "0.0.0"
}
package com.example.generator2

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val API_key = "5ca5814f-74a8-46c1-ab17-da3101e88888"

@HiltAndroidApp
class App : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        // Initialize YandexMetrica

        GlobalScope.launch(Dispatchers.IO) {
            println("Запуск Yandex Metrika")
            val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build()
            YandexMetrica.activate(this@App, config)
            YandexMetrica.enableActivityAutoTracking(this@App)
            YandexMetrica.reportEvent("Запуск")
        }

        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this, PermissionScreenActivity::class.java)
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

    }

}


//
//        //Sherlock.init(this) //Initializing Sherlock
//
//        //Bugsnag.start(this)
//        //BugsnagPerformance.start(this)
//    }






package com.example.generator2

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

val API_key = "5ca5814f-74a8-46c1-ab17-da3101e88888"

lateinit var application: Application

var startTimeAplication = System.currentTimeMillis()


@HiltAndroidApp
class App : Application() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {

        Timber.plant(Timber.DebugTree())
        Timber.tag("Время работы").i("Запуск APP")

        super.onCreate()
        Timber.tag("Время работы").i("Запуск APP1")

        startTimeAplication = System.currentTimeMillis()

        application = this

            //DebugProbes.install()

//        GlobalScope.launch(Dispatchers.IO) {
//            println("Запуск Yandex Metrika")
//            val config = YandexMetricaConfig.newConfigBuilder(API_key).withLogs().build()
//            YandexMetrica.activate(this@App, config)
//            YandexMetrica.enableActivityAutoTracking(this@App)
//            YandexMetrica.reportEvent("Запуск")
//        }

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






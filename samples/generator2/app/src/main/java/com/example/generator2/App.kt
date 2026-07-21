package com.example.generator2

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import timber.log.Timber

val API_key = "5ca5814f-74a8-46c1-ab17-da3101e88888"

@HiltAndroidApp
class App : Application() {

    companion object{
        lateinit var application: Application
        var startTimeAplication = System.currentTimeMillis()
    }

    override fun onCreate() {

        Timber.plant(Timber.DebugTree())
        Timber.tag("Время работы").i("Запуск APP")
        super.onCreate()

        startTimeAplication = System.currentTimeMillis()

        application = this

        //DebugProbes.install()

        initAppMetrica()

        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this, PermissionScreenActivity::class.java)
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

    }

    /**
     * Активация AppMetrica. Обязана выполняться синхронно в Application.onCreate,
     * иначе события до вызова activate() теряются, а нативные краши не перехватываются.
     */
    private fun initAppMetrica() {
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        val config = AppMetricaConfig.newConfigBuilder(API_key)
            .withCrashReporting(true)
            .withNativeCrashReporting(true)
            .apply { if (isDebuggable) withLogs() }
            .build()

        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)

        Timber.tag("AppMetrica").i("Активирована")
    }

}

package com.example.generator2

import android.app.Application
import com.bugsnag.android.performance.BugsnagPerformance
import com.singhajit.sherlock.core.Sherlock
import dagger.hilt.android.HiltAndroidApp
import com.bugsnag.android.Bugsnag
import jp.wasabeef.takt.Seat
import jp.wasabeef.takt.Takt

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Sherlock.init(this) //Initializing Sherlock

        Bugsnag.start(this)
        BugsnagPerformance.start(this)

        Takt.stock(this)
            .seat(Seat.TOP_RIGHT)
    }
}





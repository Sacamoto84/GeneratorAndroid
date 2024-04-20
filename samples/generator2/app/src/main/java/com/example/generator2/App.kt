package com.example.generator2

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!PermissionStorage.hasPermissions(this)) {
            val intent = Intent(this, PermissionScreenActivity::class.java)
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            //finish()
        }


    }
//
//        //Sherlock.init(this) //Initializing Sherlock
//
//        //Bugsnag.start(this)
//        //BugsnagPerformance.start(this)
//    }
}





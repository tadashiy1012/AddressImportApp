package jp.yama.addressimportapp

import android.app.Application
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("yama", "application on create!")
        AppState.onCreateApplication(applicationContext)
    }

}
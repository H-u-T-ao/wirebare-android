package top.sankokomi.wirebare.ui.app

import android.app.Application
import top.sankokomi.wirebare.ui.util.Global

class WireBareUIApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Global.attach(applicationContext)
    }

}
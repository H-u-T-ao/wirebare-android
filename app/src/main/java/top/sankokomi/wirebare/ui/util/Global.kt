package top.sankokomi.wirebare.ui.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object Global {

    lateinit var appContext: Context
        private set

    fun attach(context: Context) {
        appContext = context
    }

}
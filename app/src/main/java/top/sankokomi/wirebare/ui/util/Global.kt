package top.sankokomi.wirebare.ui.util

import android.content.Context

object Global {

    lateinit var appContext: Context
        private set

    fun attach(context: Context) {
        appContext = context
    }

}
package top.sankokomi.wirebare.ui.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun showToast(msg: String, time: Int = Toast.LENGTH_SHORT) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        Toast.makeText(Global.appContext, msg, time).show()
    } else {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(Global.appContext, msg, time).show()
        }
    }
}
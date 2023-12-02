package top.sankokomi.wirebare.ui.util

import android.view.View
import android.view.Window
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val statusBarHeightDp: Dp
    @Suppress("InternalInsetResource", "DiscouragedApi")
    get() = Global.appContext.resources?.run {
        val id = getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        (getDimensionPixelSize(id) / displayMetrics.density + 0.5F).dp
    } ?: 0.dp

/**
 * 隐藏状态栏
 * */
@Suppress("DEPRECATION")
fun Window.hideStatusBar() {
    decorView.systemUiVisibility =
        decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

/**
 * 显示状态栏
 * */
@Suppress("DEPRECATION")
fun Window.showStatusBar() {
    decorView.systemUiVisibility =
        decorView.systemUiVisibility and (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                ).inv()
}

/**
 * 隐藏导航栏
 * */
@Suppress("DEPRECATION")
fun Window.hideNavigationBar() {
    decorView.systemUiVisibility =
        decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
}

/**
 * 显示导航栏
 * */
@Suppress("DEPRECATION")
fun Window.showNavigationBar() {
    decorView.systemUiVisibility =
        decorView.systemUiVisibility and (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                ).inv()
}
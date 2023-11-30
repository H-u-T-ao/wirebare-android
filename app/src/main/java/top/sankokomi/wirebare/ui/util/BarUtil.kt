package top.sankokomi.wirebare.ui.util

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

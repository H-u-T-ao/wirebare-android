package top.sankokomi.wirebare.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import androidx.core.content.getSystemService

fun copyTextToClipBoard(s: String): Boolean {
    // 获取系统剪贴板
    val clipboard = Global.appContext.getSystemService<ClipboardManager>()
    // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
    val clipData = ClipData.newPlainText(null, s)
    // 把数据集设置（复制）到剪贴板
    return clipboard?.let {
        it.setPrimaryClip(clipData)
        true
    } ?: false
}
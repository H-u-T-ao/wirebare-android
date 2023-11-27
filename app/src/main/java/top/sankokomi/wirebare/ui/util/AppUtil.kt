package top.sankokomi.wirebare.ui.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class AppData(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean
)

fun requireAppDataList(filter: (AppData) -> Boolean = { true }): List<AppData> {
    return Global.appContext.packageManager.getInstalledApplications(
        PackageManager.MATCH_UNINSTALLED_PACKAGES
    ).mapNotNull {
        val appData = AppData(
            Global.appContext.packageManager.getApplicationLabel(it).toString(),
            it.packageName,
            it.flags and ApplicationInfo.FLAG_SYSTEM != 0
        )
        if (filter(appData)) {
            appData
        } else {
            null
        }
    }
}
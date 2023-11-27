package top.sankokomi.wirebare.ui.accesscontrol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.datastore.AppProxyData
import top.sankokomi.wirebare.ui.datastore.AppProxyDataStore
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.resources.SmallColorfulText
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.requireAppDataList

@Composable
fun AccessControlUI.AccessControlUIPage() {
    var isShowSystemApp by remember { mutableStateOf(false) }
    val appList = remember { mutableStateListOf<AppData>() }
    val appProxyList = remember { mutableStateListOf<AppProxyData>() }
    val rememberScope = rememberCoroutineScope()
    LaunchedEffect(isShowSystemApp) {
        appList.clear()
        appProxyList.clear()
        appList.addAll(
            withContext(Dispatchers.Default) {
                requireAppDataList {
                    if (it.packageName == Global.appContext.packageName) {
                        false
                    } else if (!isShowSystemApp) {
                        !it.isSystemApp
                    } else {
                        true
                    }
                }
            }
        )
        appProxyList.addAll(
            withContext(Dispatchers.IO) {
                AppProxyDataStore.first(
                    appList.map { it.packageName },
                    false
                )
            }
        )
    }
    Column {
        AppTitleBar(
            text = "访问控制"
        ) {
            RealRow(
                modifier = Modifier.clickable {
                    isShowSystemApp = !isShowSystemApp
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "显示系统应用")
                Checkbox(
                    checked = isShowSystemApp,
                    onCheckedChange = null
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(appProxyList.size) { index ->
                val appData = appList[index]
                val appProxyData = appProxyList[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            rememberScope.launch(Dispatchers.IO) {
                                appProxyList[index] = appProxyData.copy(
                                    access = !appProxyData.access
                                )
                                AppProxyDataStore.save(appProxyList[index])
                            }
                        }
                ) {
                    SmallColorfulText(
                        mainText = appData.appName,
                        subText = appData.packageName,
                        backgroundColor = Purple80,
                        textColor = Color.Black
                    )
                    Checkbox(
                        checked = appProxyData.access,
                        onCheckedChange = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    )
                }
            }
        }
    }
}
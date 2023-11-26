package top.sankokomi.wirebare.ui.accesscontrol

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.datastore.AppProxyAccessControl
import top.sankokomi.wirebare.ui.datastore.AppProxyAccessControlDataStore
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.SmallColorfulText
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.requireAppDataList

@Composable
fun AccessControlUI.AccessControlUIPage() {
    var appList by remember { mutableStateOf(listOf<AppData>()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            appList = requireAppDataList().filter {
                !it.isSystemApp
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(appList.size) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                SmallColorfulText(
                    mainText = appList[index].appName,
                    subText = appList[index].packageName,
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
                var checked by remember {
                    mutableStateOf(runBlocking {
                        AppProxyAccessControlDataStore.load(
                            appList[index].packageName,
                            false
                        )
                    }.access)
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        runBlocking {
                            AppProxyAccessControlDataStore.save(
                                AppProxyAccessControl(
                                    appList[index].packageName,
                                    !checked
                                )
                            )
                        }
                        checked = !checked
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
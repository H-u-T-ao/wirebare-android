package top.sankokomi.wirebare.ui.accesscontrol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.datastore.AccessControlDataStore
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.resources.AppCheckBoxItemMenuPopup
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.SmallColorfulText
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.requireAppDataList

@Composable
fun AccessControlUI.AccessControlUIPage() {
    val appList = remember { mutableStateListOf<AppData>() }
    val accessControlList = remember { mutableStateListOf<Boolean>() }
    var accessCount by remember { mutableIntStateOf(0) }
    val showSystemAppItem = remember {
        mutableStateOf("显示系统应用")
    } to remember {
        mutableStateOf(ProxyPolicyDataStore.showSystemApp.value)
    }
    val selectAllAppItem = remember {
        mutableStateOf("全选")
    } to remember {
        mutableStateOf(false)
    }
    val listOperateMutex = remember { Mutex(false) }
    val rememberScope = rememberCoroutineScope()
    LaunchedEffect(selectAllAppItem.second.value) {
        listOperateMutex.lock()
        // 当全选选项被修改时
        val isSelectAllApp = selectAllAppItem.second.value
        if (isSelectAllApp && accessCount < accessControlList.size) {
            // 若新选项是全选且当前没有全选
            withContext(Dispatchers.IO) {
                AccessControlDataStore.emitAll(
                    appList.map {
                        it.packageName to true
                    }
                )
            }
            accessControlList.replaceAll { true }
            accessCount = accessControlList.size
        } else if (!isSelectAllApp && accessCount >= accessControlList.size) {
            // 若新选项是全不选且当前不是全不选
            withContext(Dispatchers.IO) {
                AccessControlDataStore.emitAll(
                    appList.map {
                        it.packageName to false
                    }
                )
            }
            accessControlList.replaceAll { false }
            accessCount = 0
        }
        listOperateMutex.unlock()
    }
    LaunchedEffect(showSystemAppItem.second.value) {
        listOperateMutex.lock()
        // 当是否显示系统应用选项被修改时
        val showSystemApp = showSystemAppItem.second.value
        // 持久化当前是否显示系统应用
        ProxyPolicyDataStore.showSystemApp.value = showSystemApp
        accessCount = 0
        appList.clear()
        accessControlList.clear()
        val aList = withContext(Dispatchers.Default) {
            requireAppDataList {
                if (it.packageName == Global.appContext.packageName) {
                    false
                } else if (!showSystemApp) {
                    !it.isSystemApp
                } else {
                    true
                }
            }
        }
        val acList = withContext(Dispatchers.IO) {
            AccessControlDataStore.collectAll(
                aList.map { it.packageName }
            )
        }
        var count = 0
        acList.onEach {
            if (it) count++
        }
        accessCount = count
        appList.addAll(aList)
        accessControlList.addAll(acList)
        listOperateMutex.unlock()
    }
    LaunchedEffect(accessCount) {
        listOperateMutex.lock()
        selectAllAppItem.second.value = accessCount == accessControlList.size
        listOperateMutex.unlock()
    }
    RealColumn {
        AppTitleBar(
            text = "访问控制"
        ) {
            AppCheckBoxItemMenuPopup(
                itemList = listOf(
                    showSystemAppItem,
                    selectAllAppItem
                )
            )
        }
        if (accessControlList.isEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Purple80,
                trackColor = Color.Transparent
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(accessControlList.size) { index ->
                val appData = appList[index]
                val accessControl = accessControlList[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            rememberScope.launch(Dispatchers.IO) {
                                listOperateMutex.lock()
                                AccessControlDataStore.emit(appData.packageName to !accessControl)
                                withContext(Dispatchers.Main) {
                                    accessControlList[index] = !accessControl
                                    if (!accessControl) accessCount++ else accessCount--
                                }
                                listOperateMutex.unlock()
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
                        checked = accessControl,
                        onCheckedChange = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
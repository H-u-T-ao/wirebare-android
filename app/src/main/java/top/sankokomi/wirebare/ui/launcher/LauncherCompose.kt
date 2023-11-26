package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.core.common.ProxyStatus
import top.sankokomi.wirebare.ui.accesscontrol.AccessControlUI
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.Purple40
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.PurpleGrey40
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.showToast

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherUI.WireBareUIPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
        val urlList = remember { mutableStateListOf<String>() }
        when (wireBareStatus) {
            ProxyStatus.DEAD -> {
                LargeColorfulText(
                    mainText = "已停止",
                    subText = "点此启动",
                    backgroundColor = PurpleGrey40,
                    textColor = Color.White
                ) {
                    startProxy({ _: ProxyStatus, newStatus: ProxyStatus ->
                        wireBareStatus = newStatus
                    }, {
                        urlList.add(it)
                    })
                }
            }
            ProxyStatus.ACTIVE -> {
                LargeColorfulText(
                    mainText = "已启动",
                    subText = "点此停止",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                ) {
                    stopProxy()
                }
            }
            else -> {
                LargeColorfulText(
                    mainText = "正在停止",
                    subText = "请稍后",
                    backgroundColor = Purple40,
                    textColor = Color.White
                ) {
                    stopProxy()
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LargeColorfulText(
            mainText = "设置",
            subText = "访问控制",
            backgroundColor = Purple80,
            textColor = Color.Black
        ) {
            startActivity(Intent(this@WireBareUIPage, AccessControlUI::class.java))
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(urlList.size) {
                Text(
                    text = urlList[it],
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onLongClick = {
                                if (copyTextToClipBoard(urlList[it])) {
                                    showToast("已复制到剪贴板")
                                }
                            }
                        ) {
                            showToast("长按以复制到剪贴板")
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}
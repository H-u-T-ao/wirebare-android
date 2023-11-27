package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.common.ProxyStatus
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.ui.accesscontrol.AccessControlUI
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.Purple40
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.PurpleGrey40
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.showToast

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherUI.WireBareUIPage() {
    Column {
        AppTitleBar {
        }
        HorizontalPager(
            state = rememberPagerState { 2 },
            beyondBoundsPageCount = 2
        ) {
            when (it) {
                0 -> PageControlCenter()
                1 -> PageProxyResult()
            }
        }
    }
}

@Composable
private fun LauncherUI.PageControlCenter() {
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
    var isBanFilter by remember { mutableStateOf(false) }
    val rememberScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            isBanFilter = ProxyPolicyDataStore.load(
                ProxyPolicyDataStore.BAN_AUTO_FILTER
            ) ?: false
        }
        proxyStatusFlow.collect {
            wireBareStatus = it
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .scrollable(rememberScrollState(), Orientation.Vertical)
            .padding(horizontal = 24.dp)
    ) {
        val mainText: String
        val subText: String
        val backgroundColor: Color
        val textColor: Color
        val onClick: () -> Unit
        when (wireBareStatus) {
            ProxyStatus.DEAD -> {
                mainText = "已停止"
                subText = "点此启动"
                backgroundColor = PurpleGrey40
                textColor = Color.White
                onClick = ::startProxy
            }

            ProxyStatus.STARTING -> {
                mainText = "正在启动"
                subText = "请稍后"
                backgroundColor = Purple40
                textColor = Color.White
                onClick = ::stopProxy
            }

            ProxyStatus.ACTIVE -> {
                mainText = "已启动"
                subText = "点此停止"
                backgroundColor = Purple80
                textColor = Color.Black
                onClick = ::stopProxy
            }

            ProxyStatus.DYING -> {
                mainText = "正在停止"
                subText = "请稍后"
                backgroundColor = Purple40
                textColor = Color.White
                onClick = ::stopProxy
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
        ) {
            LargeColorfulText(
                mainText = mainText,
                subText = subText,
                backgroundColor = backgroundColor,
                textColor = textColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    startActivity(
                        Intent(
                            this@PageControlCenter,
                            AccessControlUI::class.java
                        )
                    )
                }
        ) {
            LargeColorfulText(
                mainText = "访问控制",
                subText = "配置代理应用",
                backgroundColor = Purple80,
                textColor = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        val afMainText: String
        val afSubText: String
        val afBackgroundColor: Color
        val afTextColor: Color
        if (isBanFilter) {
            afMainText = "自动过滤已停用"
            afSubText = "将显示代理到的所有请求"
            afBackgroundColor = PurpleGrey40
            afTextColor = Color.White
        } else {
            afMainText = "自动过滤已启用"
            afSubText = "将会自动过滤无法解析的请求"
            afBackgroundColor = Purple80
            afTextColor = Color.Black
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    isBanFilter = !isBanFilter
                    rememberScope.launch {
                        withContext(Dispatchers.IO) {
                            ProxyPolicyDataStore.save(
                                ProxyPolicyDataStore.BAN_AUTO_FILTER,
                                isBanFilter
                            )
                        }
                    }
                }
        ) {
            LargeColorfulText(
                mainText = afMainText,
                subText = afSubText,
                backgroundColor = afBackgroundColor,
                textColor = afTextColor
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherUI.PageProxyResult() {
    var isBanFilter by remember { mutableStateOf(false) }
    val urlList = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        proxyStatusFlow.collect {
            if (it == ProxyStatus.ACTIVE) {
                urlList.clear()
                isBanFilter = withContext(Dispatchers.IO) {
                    ProxyPolicyDataStore.load(ProxyPolicyDataStore.BAN_AUTO_FILTER)
                } ?: false
            }
        }
    }
    LaunchedEffect(Unit) {
        requestFlow.collect {
            if (!isBanFilter) {
                if (it.host == Request.UNKNOWN_HOST) return@collect
                if (it.path == Request.UNKNOWN_PATH) return@collect
            }
            urlList.add(it.url)
        }
    }
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
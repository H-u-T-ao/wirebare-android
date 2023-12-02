package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.core.common.ProxyStatus
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.core.interceptor.response.Response
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.accesscontrol.AccessControlUI
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.resources.AppNavigationBar
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.ImageButton
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.Purple40
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.PurpleGrey40
import top.sankokomi.wirebare.ui.resources.SmallColorfulText
import top.sankokomi.wirebare.ui.wireinfo.WireInfoUI

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherUI.WireBareUIPage() {
    val pagerState = rememberPagerState { 3 }
    val painterControlRes = painterResource(R.drawable.ic_wirebare)
    val painterRequestRes = painterResource(R.drawable.ic_request)
    val painterResponseRes = painterResource(R.drawable.ic_response)
    Column {
        AppTitleBar()
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 3,
            modifier = Modifier.weight(1F)
        ) {
            when (it) {
                0 -> PageControlCenter()
                1 -> PageProxyRequestResult()
                2 -> PageProxyResponseResult()
            }
        }
        AppNavigationBar(
            pagerState = pagerState,
            navigationItems = listOf(
                (painterControlRes to "控制中心") to (painterControlRes to "控制中心"),
                (painterRequestRes to "REQUEST") to (painterRequestRes to "REQUEST"),
                (painterResponseRes to "RESPONSE") to (painterResponseRes to "RESPONSE")
            )
        )
    }
}

@Composable
private fun LauncherUI.PageControlCenter() {
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
    val isBanFilter by ProxyPolicyDataStore.banAutoFilter.collectAsState()
    LaunchedEffect(Unit) {
        proxyStatusFlow.collect {
            wireBareStatus = it
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(6.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
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
                        ProxyPolicyDataStore.banAutoFilter.value = !isBanFilter
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
}

@Composable
private fun LauncherUI.PageProxyRequestResult() {
    val isBanFilter by ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val requestList = remember { mutableStateListOf<Request>() }
    LaunchedEffect(Unit) {
        requestFlow.collect {
            if (!isBanFilter) {
                if (!it.isHttp) return@collect
            }
            requestList.add(it)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(requestList.size) { index ->
                val request = requestList[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            startActivity(
                                Intent(
                                    this@PageProxyRequestResult,
                                    WireInfoUI::class.java
                                ).apply {
                                    putExtra("request", request)
                                }
                            )
                        }
                ) {
                    SmallColorfulText(
                        mainText = request.url ?: "",
                        subText = request.method ?: "",
                        backgroundColor = Purple80,
                        textColor = Color.Black
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd)
                .shadow(1.dp, RoundedCornerShape(6.dp), true)
                .background(Purple80)
                .clickable {
                    requestList.clear()
                }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            ImageButton(
                painter = painterResource(id = R.drawable.ic_clear),
                str = "清空"
            )
        }
    }
}

@Composable
private fun LauncherUI.PageProxyResponseResult() {
    val isBanFilter by ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val responseList = remember { mutableStateListOf<Response>() }
    LaunchedEffect(Unit) {
        responseFlow.collect {
            if (!isBanFilter) {
                if (!it.isHttp) return@collect
            }
            responseList.add(it)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(responseList.size) { index ->
                val response = responseList[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            startActivity(
                                Intent(
                                    this@PageProxyResponseResult,
                                    WireInfoUI::class.java
                                ).apply {
                                    putExtra("response", response)
                                }
                            )
                        }
                ) {
                    SmallColorfulText(
                        mainText = response.request?.url ?: "",
                        subText = response.formatHead?.getOrNull(0) ?: "",
                        backgroundColor = Purple80,
                        textColor = Color.Black
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd)
                .shadow(1.dp, RoundedCornerShape(6.dp), true)
                .background(Purple80)
                .clickable {
                    responseList.clear()
                }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            ImageButton(
                painter = painterResource(id = R.drawable.ic_clear),
                str = "清空"
            )
        }
    }
}
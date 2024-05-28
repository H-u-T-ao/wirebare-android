package top.sankokomi.wirebare.ui.wireinfo

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.ui.resources.AppStatusBar
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.showToast

@Composable
fun WireInfoUI.WireInfoUIPage(
    request: HttpRequest,
    sessionId: String
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            AppStatusBar()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "目的 IP 地址",
                    subText = request.destinationAddress ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "来源端口号",
                    subText = request.sourcePort?.toUShort()?.toString() ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "目的端口号",
                    subText = request.destinationPort?.toUShort()?.toString() ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "URL 链接",
                    subText = request.url ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black,
                    onLongClick = {
                        val url = request.url
                        if (!url.isNullOrBlank()) {
                            copyTextToClipBoard(url)
                            showToast("已复制 URL")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 请求方法",
                    subText = request.method ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 版本",
                    subText = request.httpVersion ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "INTERNAL SESSION KEY",
                    subText = sessionId,
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 请求头",
                    subText = request.formatHead?.joinToString("\n\n") ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            DataViewer(sessionId)
        }
    }
}

@Composable
fun WireInfoUI.WireInfoUIPage(
    response: HttpResponse,
    sessionId: String
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            AppStatusBar()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "目的 IP 地址",
                    subText = response.destinationAddress ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "来源端口",
                    subText = response.sourcePort?.toUShort()?.toString() ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "目的端口",
                    subText = response.destinationPort?.toUShort()?.toString() ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "URL 链接",
                    subText = response.url ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black,
                    onLongClick = {
                        val url = response.url
                        if (!url.isNullOrBlank()) {
                            copyTextToClipBoard(url)
                            showToast("已复制 URL")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 版本",
                    subText = response.httpVersion ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 响应状态码",
                    subText = response.rspStatus ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "INTERNAL SESSION ID",
                    subText = sessionId,
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 响应头",
                    subText = response.formatHead?.joinToString("\n\n") ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
            DataViewer(sessionId)
        }
    }
}

@Composable
private fun WireInfoUI.DataViewer(sessionId: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "解析为 HTML",
            subText = "将报文作为 HTML 文本进行解析",
            backgroundColor = Purple80,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.DirectHtml.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "gzip 解压缩并解析为 HTML",
            subText = "将报文作为被 gzip 压缩的 HTML 文本进行解析",
            backgroundColor = Purple80,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.GzipHtml.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "brotli 解压缩并解析为 HTML",
            subText = "将报文作为被 brotli 压缩的 HTML 文本进行解析",
            backgroundColor = Purple80,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.BrotliHtml.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "解析为图片",
            subText = "将报文作为图片数据进行解析",
            backgroundColor = Purple80,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.DirectImage.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "gzip 解压缩并解析为图片",
            subText = "将报文作为被 gzip 压缩的图片数据进行解析",
            backgroundColor = Purple80,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.GzipImage.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "brotli 解压缩并解析为图片",
            subText = "将报文作为被 brotli 压缩的图片数据进行解析",
            backgroundColor = Purple80,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.BrotliImage.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}
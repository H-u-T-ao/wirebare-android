package top.sankokomi.wirebare.ui.wireinfo

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
    request: HttpRequest
) {
    Column {
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
                    mainText = "DESTINATION IP ADDRESS",
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
                    mainText = "SOURCE PORT",
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
                    mainText = "DESTINATION PORT",
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
                    mainText = "URL",
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
                    mainText = "METHOD",
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
                    mainText = "HTTP VERSION",
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
                    mainText = "REQUEST HEADER",
                    subText = request.formatHead?.joinToString("\n\n") ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
        }
    }
}

@Composable
fun WireInfoUI.WireInfoUIPage(
    response: HttpResponse
) {
    Column {
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
                    mainText = "DESTINATION IP ADDRESS",
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
                    mainText = "SOURCE PORT",
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
                    mainText = "DESTINATION PORT",
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
                    mainText = "URL",
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
                    mainText = "HTTP VERSION",
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
                    mainText = "STATUS",
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
                    mainText = "RESPONSE HEADER",
                    subText = response.formatHead?.joinToString("\n\n") ?: "",
                    backgroundColor = Purple80,
                    textColor = Color.Black
                )
            }
        }
    }
}
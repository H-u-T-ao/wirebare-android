package top.sankokomi.wirebare.ui.wireinfo

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.core.interceptor.response.Response
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.Purple80

@Composable
fun WireInfoUI.WireInfoUIPage(
    request: Request
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
        ) {
            LargeColorfulText(
                mainText = "URL",
                subText = request.url ?: "",
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
                mainText = "ORIGIN REQUEST MESSAGE",
                subText = request.formatHead?.joinToString("\n\n") ?: "",
                backgroundColor = Purple80,
                textColor = Color.Black
            )
        }
    }
}

@Composable
fun WireInfoUI.WireInfoUIPage(
    response: Response
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
        ) {
            LargeColorfulText(
                mainText = "URL",
                subText = response.request?.url ?: "",
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
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
        ) {
            LargeColorfulText(
                mainText = "ORIGIN RESPONSE MESSAGE",
                subText = response.originRsp ?: "",
                backgroundColor = Purple80,
                textColor = Color.Black
            )
        }
    }
}
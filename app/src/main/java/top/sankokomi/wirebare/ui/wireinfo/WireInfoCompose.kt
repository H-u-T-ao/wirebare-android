package top.sankokomi.wirebare.ui.wireinfo

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.Purple80

@Composable
fun WireInfoUI.WireInfoUIPage(
    request: Request
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .scrollable(rememberScrollState(), Orientation.Vertical)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
        ) {
            LargeColorfulText(
                mainText = "URL",
                subText = request.url,
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
                subText = request.method,
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
                subText = request.httpVersion,
                backgroundColor = Purple80,
                textColor = Color.Black
            )
        }
    }
}
package top.sankokomi.wirebare.ui.wireinfo

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import top.sankokomi.wirebare.ui.util.decodeBitmap
import top.sankokomi.wirebare.ui.util.decodeBodyBytes
import top.sankokomi.wirebare.ui.util.decodeBrotliBitmap
import top.sankokomi.wirebare.ui.util.decodeBrotliBodyBytes
import top.sankokomi.wirebare.ui.util.decodeGzipBitmap
import top.sankokomi.wirebare.ui.util.decodeGzipBodyBytes

enum class DetailMode {
    DirectHtml,
    GzipHtml,
    BrotliHtml,
    DirectImage,
    GzipImage,
    BrotliImage
}

@Composable
fun WireDetailUI.LoadDetail(
    sessionId: String,
    mode: Int
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (mode) {
            DetailMode.DirectHtml.ordinal -> {
                DirectHtml(sessionId)
            }

            DetailMode.GzipHtml.ordinal -> {
                GzipHtml(sessionId)
            }

            DetailMode.BrotliHtml.ordinal -> {
                BrotliHtml(sessionId)
            }

            DetailMode.DirectImage.ordinal -> {
                DirectImage(sessionId)
            }

            DetailMode.GzipImage.ordinal -> {
                GzipImage(sessionId)
            }

            DetailMode.BrotliImage.ordinal -> {
                BrotliImage(sessionId)
            }
        }
    }
}

@Composable
fun WireDetailUI.DirectHtml(sessionId: String) {
    var html by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val bytes = decodeBodyBytes(sessionId) ?: return@LaunchedEffect
        html = String(bytes, 0, bytes.size)
    }
    val text = html
    if (text.isNotBlank()) {
        AndroidView(
            factory = {
                WebView(it)
            },
            modifier = Modifier
                .fillMaxSize(),
            update = { web ->
                web.webViewClient = WebViewClient()
                web.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null)
            }
        )
    }
}

@Composable
fun WireDetailUI.GzipHtml(sessionId: String) {
    var html by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val bytes = decodeGzipBodyBytes(sessionId) ?: return@LaunchedEffect
        html = String(bytes, 0, bytes.size)
    }
    val text = html
    if (text.isNotBlank()) {
        AndroidView(
            factory = {
                WebView(it)
            },
            modifier = Modifier
                .fillMaxSize(),
            update = { web ->
                web.webViewClient = WebViewClient()
                web.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null)
            }
        )
    }
}

@Composable
fun WireDetailUI.BrotliHtml(sessionId: String) {
    var html by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val bytes = decodeBrotliBodyBytes(sessionId) ?: return@LaunchedEffect
        html = String(bytes, 0, bytes.size)
    }
    val text = html
    if (text.isNotBlank()) {
        AndroidView(
            factory = {
                WebView(it)
            },
            modifier = Modifier
                .fillMaxSize(),
            update = { web ->
                web.webViewClient = WebViewClient()
                web.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null)
            }
        )
    }
}

@Composable
fun WireDetailUI.DirectImage(sessionId: String) {
    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        bitmap = decodeBitmap(sessionId)
    }
    val b = bitmap
    if (b != null) {
        Image(
            bitmap = b.asImageBitmap(),
            modifier = Modifier
                .fillMaxSize(),
            contentDescription = null
        )
    }
}

@Composable
fun WireDetailUI.GzipImage(sessionId: String) {
    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        bitmap = decodeGzipBitmap(sessionId)
    }
    val b = bitmap
    if (b != null) {
        Image(
            bitmap = b.asImageBitmap(),
            modifier = Modifier
                .fillMaxSize(),
            contentDescription = null
        )
    }
}

@Composable
fun WireDetailUI.BrotliImage(sessionId: String) {
    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        bitmap = decodeBrotliBitmap(sessionId)
    }
    val b = bitmap
    if (b != null) {
        Image(
            bitmap = b.asImageBitmap(),
            modifier = Modifier
                .fillMaxSize(),
            contentDescription = null
        )
    }
}
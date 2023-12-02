package top.sankokomi.wirebare.core.interceptor.request

import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

/**
 * 请求头格式化拦截器，负责格式化 [RequestChain.request]
 * */
class RequestHeaderParseInterceptor : RequestInterceptor() {

    override fun onRequest(request: Request, buffer: ByteBuffer) {
        kotlin.runCatching {
            val requestString = String(buffer.array(), buffer.position(), buffer.remaining())
            request.isHttp = requestString.isHttp
            if (!request.isHttp) return
            val headerString = requestString.substringBeforeLast("\r\n\r\n")
            val headers = headerString.split("\r\n")
            val requestLine = headers[0].split(" ".toRegex())
            request.originHead = requestString
            request.formatHead = headers.filter { it.isNotBlank() }
            request.method = requestLine[0]
            request.httpVersion = requestLine[requestLine.size - 1]
            request.path = headers[0].replace(requestLine[0], "")
                .replace(requestLine[requestLine.size - 1], "")
                .trim()
            headers.forEach { msg->
                val hostIndex = msg.indexOf("Host: ")
                if(hostIndex != -1) {
                    request.host = msg.substring(hostIndex + 6)
                }
            }
        }.onFailure {
            WireBareLogger.error("构造 HTTP 请求时出现错误")
        }
    }

    override fun onRequestFinished() {
    }

    private val String.isHttp: Boolean
        get() {
            if (length < 3) return false
            when (substring(0, 3)) {
                "GET", "PUT" -> return true
            }
            if (length < 4) return false
            when (substring(0, 4)) {
                "HEAD", "POST" -> return true
            }
            if (length < 5) return false
            when (substring(0, 5)) {
                "TRACE" -> return true
            }
            if (length < 6) return false
            when(substring(0, 6)) {
                "DELETE" -> return true
            }
            if (length < 7) return false
            when(substring(0, 7)) {
                "OPTIONS", "CONNECT" -> return true
            }
            return false
        }

}
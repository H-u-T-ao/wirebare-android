package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

/**
 * 请求头格式化拦截器，负责格式化 [RequestChain.request]
 * */
class RequestHeaderParseInterceptor : RequestInterceptor() {

    override fun onRequest(request: Request, buffer: ByteBuffer) {
        request._isHttp = buffer.isHttp
        if (request._isHttp == false || request._isHttp == null) return
        kotlin.runCatching {
            val headerString = String(buffer.array(), buffer.position(), buffer.remaining())
            val headers = headerString.split("\r\n")
            val requestLine = headers[0].split(" ".toRegex())
            request._method = requestLine[0]
            request._httpVersion = requestLine[requestLine.size - 1]
            request._path = headers[0].replace(requestLine[0], "")
                .replace(requestLine[requestLine.size - 1], "")
                .trim()
            headers.forEach { msg->
                val hostIndex = msg.indexOf("Host: ")
                if(hostIndex != -1) {
                    request._host = msg.substring(hostIndex + 6)
                }
            }
        }.onFailure {
            WireBareLogger.error("构造 HTTP 请求时出现错误")
        }
    }

    override fun onRequestFinished(request: Request) {
    }

    private val ByteBuffer.isHttp: Boolean?
        get() {
            return when (get(position()).toInt()) {
                71/* G */,
                72/* H */,
                80/* P */,
                68/* D */,
                79/* O */,
                84/* T */,
                67/* C */ -> true
                in 20..24 -> false
                else -> null
            }
        }

}
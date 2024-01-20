package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

/**
 * Http 请求头，响应头拦截器
 * */
class HttpHeaderParserInterceptor : HttpIndexedInterceptor() {

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        index: Int
    ) {
        if (index == 0) {
            kotlin.runCatching {
                val (request, _) = chain.curReqRsp(session) ?: return@runCatching
                val requestString = String(buffer.array(), buffer.position(), buffer.remaining())
                request.isHttp = requestString.isReqHttp
                if (!request.isHttp) return@runCatching
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
                headers.forEach { msg ->
                    val hostIndex = msg.indexOf("Host: ")
                    if (hostIndex != -1) {
                        request.host = msg.substring(hostIndex + 6)
                    }
                }
            }.onFailure {
                WireBareLogger.error("构造 HTTP 请求时出现错误")
            }
        }
        chain.processRequestNext(buffer, session)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        index: Int
    ) {
        if (index == 0) {
            kotlin.runCatching {
                val (request, response) = chain.curReqRsp(session) ?: return@runCatching
                response.url = request.url
                val responseString = String(buffer.array(), buffer.position(), buffer.remaining())
                response.isHttp = responseString.isRspHttp
                if (!response.isHttp) return@runCatching
                val headerString = responseString.substringBeforeLast("\r\n\r\n")
                val headers = headerString.split("\r\n")
                val responseLine = headers[0].split(" ".toRegex())
                response.originHead = headerString
                response.formatHead = headers.filter { it.isNotBlank() }
                response.httpVersion = responseLine[0]
                response.rspStatus = responseLine[1]
            }.onFailure {
                WireBareLogger.error("构造 HTTP 响应时出现错误")
            }
        }
        chain.processResponseNext(buffer, session)
    }

    private val String.isReqHttp: Boolean
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
            when (substring(0, 6)) {
                "DELETE" -> return true
            }
            if (length < 7) return false
            when (substring(0, 7)) {
                "OPTIONS", "CONNECT" -> return true
            }
            return false
        }


    private val String.isRspHttp: Boolean
        get() {
            if (length < 4) return false
            return substring(0, 4) == "HTTP"
        }

}
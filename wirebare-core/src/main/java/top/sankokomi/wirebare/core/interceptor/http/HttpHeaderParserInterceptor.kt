package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.newString
import java.nio.ByteBuffer

/**
 * Http 请求头，响应头拦截器
 * */
class HttpHeaderParserInterceptor : HttpIndexedInterceptor() {

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0 && session.isPlaintext) {
            kotlin.runCatching {
                val (request, _) = session
                val requestString = buffer.newString()
                val headerString = requestString.substringBefore("\r\n\r\n")
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
        super.onRequest(chain, buffer, session, tunnel, index)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0 && session.isPlaintext) {
            kotlin.runCatching {
                val (request, response) = session
                response.url = request.url
                val responseString = buffer.newString()
                val headerString = responseString.substringBefore("\r\n\r\n")
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
        super.onResponse(chain, buffer, session, tunnel, index)
    }
}
package top.sankokomi.wirebare.core.interceptor.response

import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

/**
 * 响应头格式化拦截器，负责格式化 [ResponseChain.response]
 * */
class ResponseHeaderParseInterceptor : ResponseInterceptor() {

    override fun onResponse(response: Response, buffer: ByteBuffer) {
        kotlin.runCatching {
            val responseString = String(buffer.array(), buffer.position(), buffer.remaining())
            response.isHttp = responseString.isHttp
            if (!response.isHttp) return
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

    override fun onResponseFinished() {
    }

    private val String.isHttp: Boolean
        get() {
            if (length < 4) return false
            return substring(0, 4) == "HTTP"
        }

}
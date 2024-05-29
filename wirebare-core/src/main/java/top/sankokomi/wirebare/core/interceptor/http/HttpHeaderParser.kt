package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.newString
import java.nio.ByteBuffer

internal fun parseHttpRequestHeader(
    buffer: ByteBuffer,
    session: HttpSession
) {
    runCatching {
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
        parseHeaderLine(headers, listOf("Host: ")) { name, content ->
            when (name) {
                "Host: " -> request.host = content
            }
        }
    }.onFailure {
        WireBareLogger.error("构造 HTTP 请求 时出现错误")
    }
}

internal fun parseHttpResponseHeader(
    buffer: ByteBuffer,
    session: HttpSession
) {
    runCatching {
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
        parseHeaderLine(
            headers,
            listOf(
                "Content-Type: ",
                "Content-Encoding: "
            )
        ) { name, content ->
            when (name) {
                "Content-Type: " -> response.contentType = content
                "Content-Encoding: " -> response.contentEncoding = content
            }
        }
    }.onFailure {
        WireBareLogger.error("构造 HTTP 响应时出现错误")
    }
}

private fun parseHeaderLine(
    headers: List<String>,
    names: List<String>,
    onFound: (name: String, content: String) -> Unit
) {
    val nameList = names.toMutableList()
    headers.forEach { msg ->
        nameList.removeAll { name ->
            val index = msg.indexOf(name)
            if (index != -1) {
                onFound(name, msg.substring(index + name.length))
                return@removeAll true
            }
            return@removeAll false
        }
    }
}
package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.net.TcpSession

data class HttpSession(
    val request: HttpRequest,
    val response: HttpResponse,
    val tcpSession: TcpSession
)
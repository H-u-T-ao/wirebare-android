package top.sankokomi.wirebare.ui.wireinfo

import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.ui.util.appendToDataCache
import java.nio.ByteBuffer

class WireBareHttpInterceptor(
    private val onRequest: (HttpRequest) -> Unit,
    private val onResponse: (HttpResponse) -> Unit
) : HttpIndexedInterceptor() {

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0) {
            onRequest(session.request)
        }
        appendToDataCache("req_${session.request.hashCode()}", buffer)
        super.onRequest(chain, buffer, session, tunnel, index)
    }

    override fun onRequestFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        super.onRequestFinished(chain, session, tunnel, index)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0) {
            onResponse(session.response)
        }
        appendToDataCache("rsp_${session.response.hashCode()}", buffer)
        super.onResponse(chain, buffer, session, tunnel, index)
    }

    override fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        super.onResponseFinished(chain, session, tunnel, index)
    }
}
package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpInterceptChain
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

class HttpInterceptChain(
    private val interceptors: List<HttpInterceptor>
) : TcpInterceptChain<HttpRequest, HttpResponse>() {

    private var interceptorIndex = -1

    override fun newInstanceReqRsp(): Pair<HttpRequest, HttpResponse> {
        return HttpRequest() to HttpResponse()
    }

    override fun processRequestNext(buffer: ByteBuffer, session: TcpSession) {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onRequest(this, buffer, session)
    }

    override fun processRequestFinishedNext(session: TcpSession) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onRequestFinished(this, session)
    }

    override fun processResponseNext(buffer: ByteBuffer, session: TcpSession) {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onResponse(this, buffer, session)
    }

    override fun processResponseFinishedNext(session: TcpSession) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onResponseFinished(this, session)
    }

    override fun processRequestFirst(buffer: ByteBuffer, session: TcpSession) {
        interceptorIndex = -1
        super.processRequestFirst(buffer, session)
    }

    override fun processRequestFinishedFirst(session: TcpSession) {
        interceptorIndex = -1
        super.processRequestFinishedFirst(session)
    }

    override fun processResponseFirst(buffer: ByteBuffer, session: TcpSession) {
        interceptorIndex = -1
        super.processResponseFirst(buffer, session)
    }

    override fun processResponseFinishedFirst(session: TcpSession) {
        interceptorIndex = -1
        super.processResponseFinishedFirst(session)
    }
}
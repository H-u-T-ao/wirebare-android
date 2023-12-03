package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

class HttpInterceptChain(
    private val interceptors: List<HttpInterceptor>
) : InterceptorChain {

    private var interceptorIndex = -1

    private val mapReqRsp = hashMapOf<TcpSession, Pair<Request, Response>>()

    fun getReqRsp(session: TcpSession): Pair<Request, Response>? {
        return mapReqRsp[session]
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

    internal fun processRequest(buffer: ByteBuffer, session: TcpSession) {
        interceptorIndex = -1
        if (!mapReqRsp.containsKey(session)) {
            mapReqRsp[session] = Request() to Response()
        }
        processRequestNext(buffer, session)
    }

    internal fun processRequestFinished(session: TcpSession) {
        interceptorIndex = -1
        processRequestFinishedNext(session)
        mapReqRsp.remove(session)
    }

    internal fun processResponse(buffer: ByteBuffer, session: TcpSession) {
        interceptorIndex = -1
        processResponseNext(buffer, session)
    }

    internal fun processResponseFinished(session: TcpSession) {
        interceptorIndex = -1
        processResponseFinishedNext(session)
    }

}
package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.net.Session
import java.nio.ByteBuffer

class HttpInterceptChain(
    private val interceptors: List<HttpInterceptor>
) : InterceptorChain {

    private var interceptorIndex = -1

    private val mapReqRsp = hashMapOf<Session, Pair<Request, Response>>()

    fun getReqRsp(session: Session): Pair<Request, Response>? {
        return mapReqRsp[session]
    }

    override fun processRequestNext(buffer: ByteBuffer, session: Session) {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onRequest(this, buffer, session)
    }

    override fun processRequestFinishedNext(session: Session) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onRequestFinished(this, session)
    }

    override fun processResponseNext(buffer: ByteBuffer, session: Session) {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onResponse(this, buffer, session)
    }

    override fun processResponseFinishedNext(session: Session) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onResponseFinished(this, session)
    }

    internal fun processRequest(buffer: ByteBuffer, session: Session) {
        interceptorIndex = -1
        this.mapReqRsp[session] = Request() to Response()
        processRequestNext(buffer, session)
    }

    internal fun processRequestFinished(session: Session) {
        interceptorIndex = -1
        processRequestFinishedNext(session)
    }

    internal fun processResponse(buffer: ByteBuffer, session: Session) {
        interceptorIndex = -1
        processResponseNext(buffer, session)
    }

    internal fun processResponseFinished(session: Session) {
        interceptorIndex = -1
        processResponseFinishedNext(session)
        this.mapReqRsp.remove(session)
    }

}
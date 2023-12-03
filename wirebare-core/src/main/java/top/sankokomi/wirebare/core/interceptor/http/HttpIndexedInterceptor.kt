package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

abstract class HttpIndexedInterceptor : HttpInterceptor {

    private val mapReqIndexed = hashMapOf<TcpSession, Int>()

    private val mapRspIndexed = hashMapOf<TcpSession, Int>()

    open fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession, index: Int) {
        chain.processRequestNext(buffer, session)
    }

    open fun onRequestFinished(chain: HttpInterceptChain, session: TcpSession, index: Int) {
        chain.processRequestFinishedNext(session)
    }

    open fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession, index: Int) {
        chain.processResponseNext(buffer, session)
    }

    open fun onResponseFinished(chain: HttpInterceptChain, session: TcpSession, index: Int) {
        chain.processResponseFinishedNext(session)
    }

    final override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        val requestIndex = (mapReqIndexed[session] ?: -1) + 1
        mapReqIndexed[session] = requestIndex
        onRequest(chain, buffer, session, requestIndex)
    }

    final override fun onRequestFinished(chain: HttpInterceptChain, session: TcpSession) {
        val requestIndex = mapReqIndexed[session] ?: -1
        onRequestFinished(chain, session, requestIndex)
        mapReqIndexed.remove(session)
    }

    final override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        val responseIndex = (mapRspIndexed[session] ?: -1) + 1
        mapReqIndexed[session] = responseIndex
        onResponse(chain, buffer, session, responseIndex)
    }

    final override fun onResponseFinished(chain: HttpInterceptChain, session: TcpSession) {
        val responseIndex = mapRspIndexed[session] ?: -1
        onResponseFinished(chain, session, responseIndex)
        mapRspIndexed.remove(session)
    }

}
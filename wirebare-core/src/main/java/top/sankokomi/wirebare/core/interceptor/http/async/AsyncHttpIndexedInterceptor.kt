package top.sankokomi.wirebare.core.interceptor.http.async

import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.util.UnsupportedCall
import java.nio.ByteBuffer

abstract class AsyncHttpIndexedInterceptor : AsyncHttpInterceptor {

    private val reqIndexMap = hashMapOf<HttpSession, Int>()
    private val rspIndexMap = hashMapOf<HttpSession, Int>()

    open suspend fun onRequest(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        chain.processRequestNext(this, buffer, session)
    }

    open suspend fun onRequestFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession,
        index: Int
    ) {
        chain.processRequestFinishedNext(this, session)
    }

    open suspend fun onResponse(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        chain.processResponseNext(this, buffer, session)
    }

    open suspend fun onResponseFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession,
        index: Int
    ) {
        chain.processResponseFinishedNext(this, session)
    }

    @UnsupportedCall
    final override suspend fun onRequest(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        reqIndexMap.compute(session) { _, value -> (value ?: -1) + 1 }
        onRequest(chain, buffer, session, reqIndexMap[session] ?: return)
    }

    @UnsupportedCall
    final override suspend fun onRequestFinished(chain: AsyncHttpInterceptChain, session: HttpSession) {
        onRequestFinished(chain, session, (reqIndexMap[session] ?: return) + 1)
        reqIndexMap.remove(session)
    }

    @UnsupportedCall
    final override suspend fun onResponse(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        rspIndexMap.compute(session) { _, value -> (value ?: -1) + 1 }
        onResponse(chain, buffer, session, rspIndexMap[session] ?: return)
    }

    @UnsupportedCall
    final override suspend fun onResponseFinished(chain: AsyncHttpInterceptChain, session: HttpSession) {
        onResponseFinished(chain, session, (rspIndexMap[session] ?: return) + 1)
        rspIndexMap.remove(session)
    }

}
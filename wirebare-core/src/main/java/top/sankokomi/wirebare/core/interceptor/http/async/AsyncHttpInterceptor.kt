package top.sankokomi.wirebare.core.interceptor.http.async

import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import java.nio.ByteBuffer

interface AsyncHttpInterceptor {
    suspend fun onRequest(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        chain.processRequestNext(this, buffer, session)
    }

    suspend fun onRequestFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession
    ) {
        chain.processRequestFinishedNext(this, session)
    }

    suspend fun onResponse(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        chain.processResponseNext(this, buffer, session)
    }

    suspend fun onResponseFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession
    ) {
        chain.processResponseFinishedNext(this, session)
    }
}
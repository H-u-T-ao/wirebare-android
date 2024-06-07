package top.sankokomi.wirebare.core.interceptor.http.async

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import java.nio.ByteBuffer

/**
 * HTTP 异步拦截器，如果不需要对要报文做出修改（例如只解析），建议使用这种拦截器，可以节约响应的时间
 *
 * @see [HttpInterceptor]
 * */
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
package top.sankokomi.wirebare.core.interceptor.http.async

import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.interceptor.http.parseHttpRequestHeader
import top.sankokomi.wirebare.core.interceptor.http.parseHttpResponseHeader
import java.nio.ByteBuffer

/**
 * Http 请求头，响应头拦截器
 * */
class AsyncHttpHeaderParserInterceptor : AsyncHttpIndexedInterceptor() {

    override suspend fun onRequest(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        if (
            index == 0 &&
            session.request.isPlaintext == true &&
            session.request.originHead == null
        ) {
            parseHttpRequestHeader(buffer, session)
        }
        super.onRequest(chain, buffer, session, index)
    }

    override suspend fun onResponse(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        if (
            index == 0 &&
            session.response.isPlaintext == true &&
            session.response.originHead == null
        ) {
            parseHttpResponseHeader(buffer, session)
        }
        super.onResponse(chain, buffer, session, index)
    }
}
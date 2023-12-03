package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * 拦截器
 *
 * @param C 拦截器责任链
 * */
interface Interceptor<C : InterceptorChain> {

    fun onRequest(chain: C, buffer: ByteBuffer, session: TcpSession)

    fun onRequestFinished(chain: C, session: TcpSession)

   fun onResponse(chain: C, buffer: ByteBuffer, session: TcpSession)

    fun onResponseFinished(chain: C, session: TcpSession)

}
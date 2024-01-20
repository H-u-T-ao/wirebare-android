package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * 拦截器
 *
 * @param CHAIN 拦截器责任链
 * */
interface Interceptor<CHAIN : InterceptorChain<*>, SESSION: Session<*>> {

    fun onRequest(chain: CHAIN, buffer: ByteBuffer, session: SESSION)

    fun onRequestFinished(chain: CHAIN, session: SESSION)

   fun onResponse(chain: CHAIN, buffer: ByteBuffer, session: SESSION)

    fun onResponseFinished(chain: CHAIN, session: SESSION)

}
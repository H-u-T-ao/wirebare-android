package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session
import java.nio.ByteBuffer

/**
 * 拦截器
 *
 * @param C 拦截器责任链
 * */
interface Interceptor<C : InterceptorChain> {

    fun onRequest(chain: C, buffer: ByteBuffer, session: Session)

    fun onRequestFinished(chain: C, session: Session)

   fun onResponse(chain: C, buffer: ByteBuffer, session: Session)

    fun onResponseFinished(chain: C, session: Session)

}
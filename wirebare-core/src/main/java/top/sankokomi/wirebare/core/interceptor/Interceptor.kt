package top.sankokomi.wirebare.core.interceptor

import java.nio.ByteBuffer

/**
 * 拦截器
 *
 * @param C 拦截器责任链
 * */
interface Interceptor<C : InterceptorChain> {

    fun onRequest(chain: C, buffer: ByteBuffer)

    fun onRequestFinished(chain: C)

   fun onResponse(chain: C, buffer: ByteBuffer)

    fun onResponseFinished(chain: C)

}
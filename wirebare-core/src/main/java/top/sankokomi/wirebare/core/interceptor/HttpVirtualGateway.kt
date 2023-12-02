package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.http.HttpHeaderParserInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import java.nio.ByteBuffer

/**
 * 虚拟网关
 * */
class HttpVirtualGateway internal constructor(
    configuration: WireBareConfiguration
) {

    private val interceptorChain: HttpInterceptChain

    init {
        val interceptors = mutableListOf<HttpInterceptor>()
        // 请求头，响应头格式化拦截器
        interceptors.add(HttpHeaderParserInterceptor())
        interceptors.addAll(
            configuration.httpInterceptorFactories.map { it.create() }
        )
        interceptorChain = HttpInterceptChain(interceptors)
    }

    internal fun onRequest(buffer: ByteBuffer) {
        interceptorChain.processRequest(buffer)
    }

    internal fun onRequestFinished() {
        interceptorChain.processRequestFinished()
    }

    internal fun onResponse(buffer: ByteBuffer) {
        interceptorChain.processResponse(buffer)
    }

    internal fun onResponseFinished() {
        interceptorChain.processResponseFinished()
    }

}
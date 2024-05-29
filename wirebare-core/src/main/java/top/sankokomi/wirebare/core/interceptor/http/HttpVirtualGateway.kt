package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.http.async.AsyncHttpHeaderParserInterceptor
import top.sankokomi.wirebare.core.interceptor.http.async.AsyncHttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.ssl.HttpSSLCodecInterceptor
import top.sankokomi.wirebare.core.interceptor.ssl.HttpSSLSniffInterceptor
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
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
        // HTTP/HTTPS 嗅探拦截器，用于判断 HTTP/HTTPS
        interceptors.add(HttpSSLSniffInterceptor())
        val jks = configuration.jks
        if (jks != null) {
            val sslDecodeInterceptor = HttpSSLCodecInterceptor(jks)
            interceptors.add(sslDecodeInterceptor)
            interceptors.addNormalInterceptors(configuration)
            interceptors.add(
                HttpFlushInterceptor(
                    sslDecodeInterceptor.requestCodec,
                    sslDecodeInterceptor.responseCodec
                )
            )
        } else {
            interceptors.addNormalInterceptors(configuration)
            interceptors.add(HttpFlushInterceptor())
        }
        interceptorChain = HttpInterceptChain(interceptors)
    }

    private fun MutableList<HttpInterceptor>.addNormalInterceptors(
        configuration: WireBareConfiguration,
    ) {
        val interceptors = this@addNormalInterceptors
        // 自定义拦截器
        interceptors.addAll(
            configuration.httpInterceptorFactories.map { it.create() }
        )
        interceptors.add(
            AsyncHttpInterceptChain(
                configuration.asyncHttpInterceptorFactories.mapTo(
                    // HTTP 请求头，响应头格式化拦截器
                    mutableListOf(AsyncHttpHeaderParserInterceptor())
                ) { it.create() }
            )
        )
    }

    fun onRequest(
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        interceptorChain.processRequestFirst(buffer, session, tunnel)
    }

    fun onRequestFinished(
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        interceptorChain.processRequestFinishedFirst(session, tunnel)
    }

    fun onResponse(
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        interceptorChain.processResponseFirst(buffer, session, tunnel)
    }

    fun onResponseFinished(
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        interceptorChain.processResponseFinishedFirst(session, tunnel)
    }

}
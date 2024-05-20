package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.common.WireBareConfiguration
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
        if (configuration.jks != null) {
            val sslDecodeInterceptor = HttpSSLCodecInterceptor(configuration)
            interceptors.add(sslDecodeInterceptor)
            // HTTP 请求头，响应头格式化拦截器
            interceptors.add(HttpHeaderParserInterceptor())
            // 自定义拦截器
            interceptors.addAll(
                configuration.httpInterceptorFactories.map { it.create() }
            )
            interceptors.add(
                HttpFlushInterceptor(
                    sslDecodeInterceptor.requestCodec,
                    sslDecodeInterceptor.responseCodec
                )
            )
        } else {
            // HTTP 请求头，响应头格式化拦截器
            interceptors.add(HttpHeaderParserInterceptor())
            // 自定义拦截器
            interceptors.addAll(
                configuration.httpInterceptorFactories.map { it.create() }
            )
            interceptors.add(HttpFlushInterceptor())
        }
        interceptorChain = HttpInterceptChain(interceptors)
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
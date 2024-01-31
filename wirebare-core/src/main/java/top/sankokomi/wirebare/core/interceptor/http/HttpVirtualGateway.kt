package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.ssl.HttpSSLCodecInterceptor
import top.sankokomi.wirebare.core.interceptor.ssl.HttpSSLRefluxInterceptor
import top.sankokomi.wirebare.core.interceptor.ssl.HttpSSLSniffInterceptor
import top.sankokomi.wirebare.core.interceptor.tcp.TcpVirtualGateway

/**
 * 虚拟网关
 * */
class HttpVirtualGateway internal constructor(
    configuration: WireBareConfiguration
): TcpVirtualGateway() {

    override val interceptorChain: HttpInterceptChain

    init {
        val interceptors = mutableListOf<HttpInterceptor>()
        // HTTP/HTTPS 嗅探拦截器，用于判断 HTTP/HTTPS
        interceptors.add(HttpSSLSniffInterceptor())
        // HTTPS 解码（解密）/编码（加密）拦截器，编码阶段需要接收回流器回流
        val sslDecodeInterceptor = HttpSSLCodecInterceptor(configuration)
        interceptors.add(sslDecodeInterceptor)
        // HTTP 请求头，响应头格式化拦截器
        interceptors.add(HttpHeaderParserInterceptor())
        // 自定义拦截器
        interceptors.addAll(
            configuration.httpInterceptorFactories.map { it.create() }
        )
        // HTTPS 编码（加密）拦截器
        interceptors.add(HttpSSLRefluxInterceptor(sslDecodeInterceptor))
        interceptorChain = HttpInterceptChain(interceptors)
    }

}
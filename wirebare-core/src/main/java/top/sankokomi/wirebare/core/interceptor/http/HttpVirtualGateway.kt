package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.VirtualGateway
import top.sankokomi.wirebare.core.interceptor.tcp.TcpVirtualGateway
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * 虚拟网关
 * */
class HttpVirtualGateway internal constructor(
    configuration: WireBareConfiguration
): TcpVirtualGateway() {

    override val interceptorChain: HttpInterceptChain

    init {
        val interceptors = mutableListOf<HttpInterceptor>()
        // 请求头，响应头格式化拦截器
        interceptors.add(HttpHeaderParserInterceptor())
        interceptors.addAll(
            configuration.httpInterceptorFactories.map { it.create() }
        )
        interceptorChain = HttpInterceptChain(interceptors)
    }

}
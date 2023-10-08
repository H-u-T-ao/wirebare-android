package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import java.nio.ByteBuffer

/**
 * 虚拟网关
 * */
class VirtualGateWay(
    configuration: WireBareConfiguration
) {

    private val requestChain: RequestChain = RequestChain(
        mutableListOf<RequestInterceptor>().apply {
            // 请求头格式化拦截器
            add(RequestHeaderParseInterceptor())
            configuration.requestInterceptorFactories.forEach {
                add(it.create())
            }
        }
    )

    private val responseChain: ResponseChain = ResponseChain(
        mutableListOf<ResponseInterceptor>().apply {
            // 请求头格式化拦截器
            add(ResponseHeaderParseInterceptor())
            configuration.responseInterceptorFactories.forEach {
                add(it.create())
            }
        }
    )

    internal fun onRequest(buffer: ByteBuffer) {
        requestChain.startProcessing(buffer)
    }

    internal fun onRequestFinished() {
        requestChain.stopProcessing()
    }

    internal fun onResponse(buffer: ByteBuffer) {
        responseChain.startProcessing(buffer)
    }

    internal fun onResponseFinished() {
        responseChain.stopProcessing()
    }

}
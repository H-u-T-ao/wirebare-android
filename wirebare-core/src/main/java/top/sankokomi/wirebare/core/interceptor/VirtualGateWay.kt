package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.request.RequestChain
import top.sankokomi.wirebare.core.interceptor.request.RequestHeaderParseInterceptor
import top.sankokomi.wirebare.core.interceptor.request.RequestInterceptor
import top.sankokomi.wirebare.core.interceptor.response.ResponseChain
import top.sankokomi.wirebare.core.interceptor.response.ResponseHeaderParseInterceptor
import top.sankokomi.wirebare.core.interceptor.response.ResponseInterceptor
import java.nio.ByteBuffer

/**
 * 虚拟网关
 * */
class VirtualGateWay internal constructor(
    configuration: WireBareConfiguration
) {

    private val requestChain: RequestChain = RequestChain(
        mutableListOf<RequestInterceptor>().apply {
            // 请求头格式化拦截器
            add(RequestHeaderParseInterceptor())
            configuration.requestInterceptorFactories.forEach {
                addAll(it.create())
            }
        }
    )

    private val responseChain: ResponseChain = ResponseChain(
        mutableListOf<ResponseInterceptor>().apply {
            // 响应头格式化拦截器
            add(ResponseHeaderParseInterceptor())
            configuration.responseInterceptorFactories.forEach {
                addAll(it.create())
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
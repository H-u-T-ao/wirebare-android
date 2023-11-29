package top.sankokomi.wirebare.core.interceptor.response

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

/**
 * 响应责任链
 *
 * @param interceptors 响应责任链
 * */
class ResponseChain internal constructor(
    private val interceptors: List<ResponseInterceptor>
) : InterceptorChain {

    private var index: Int = -1

    internal lateinit var response: Response

    /**
     * 开始处理
     *
     * @param buffer 要处理的缓冲流
     * */
    @Synchronized
    internal fun startProcessing(buffer: ByteBuffer, request: Request? = null) {
        response = Response().also {
            it.request = request
        }
        index = -1
        process(buffer)
    }

    @Synchronized
    internal fun stopProcessing() {
        if (::response.isInitialized) {
            for (interceptor in interceptors) {
                interceptor.onResponseFinished(response)
            }
        } else {
            WireBareLogger.warn("没有收到开始响应的信息")
        }
    }

    override fun process(buffer: ByteBuffer) {
        index++
        if (index >= interceptors.size) return
        interceptors[index].intercept(this, buffer)
    }

}
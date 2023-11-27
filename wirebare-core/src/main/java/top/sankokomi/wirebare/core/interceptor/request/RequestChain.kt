package top.sankokomi.wirebare.core.interceptor.request

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

/**
 * 请求责任链
 *
 * @param interceptors 请求责任链
 * */
class RequestChain internal constructor(
    private val interceptors: List<RequestInterceptor>
) : InterceptorChain {

    private var index: Int = -1

    internal lateinit var request: Request

    /**
     * 开始处理
     *
     * @param buffer 要处理的缓冲流
     * */
    @Synchronized
    internal fun startProcessing(buffer: ByteBuffer) {
        request = Request()
        index = -1
        process(buffer)
    }

    @Synchronized
    internal fun stopProcessing() {
        if (::request.isInitialized) {
            for (interceptor in interceptors) {
                interceptor.onRequestFinished(request)
            }
        } else {
            WireBareLogger.warn("没有收到开始请求的信息")
        }
    }

    override fun process(buffer: ByteBuffer) {
        index++
        if (index >= interceptors.size) return
        interceptors[index].intercept(this, buffer)
    }

}
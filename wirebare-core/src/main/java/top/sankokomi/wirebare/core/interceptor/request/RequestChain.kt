package top.sankokomi.wirebare.core.interceptor.request

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
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

    internal var request: Request? = null

    /**
     * 开始处理
     *
     * @param buffer 要处理的缓冲流
     * */
    internal fun onRequest(buffer: ByteBuffer) {
        request = Request()
        index = -1
        process(buffer)
    }

    internal fun onRequestFinished() {
        for (interceptor in interceptors) {
            interceptor.onRequestFinished()
        }
        request = null
    }

    override fun process(buffer: ByteBuffer) {
        index++
        if (index >= interceptors.size) return
        interceptors[index].intercept(this, buffer)
    }

}
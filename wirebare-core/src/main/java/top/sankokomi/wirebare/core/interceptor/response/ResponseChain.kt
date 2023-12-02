package top.sankokomi.wirebare.core.interceptor.response

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.interceptor.request.Request
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

    internal var response: Response? = null

    /**
     * 开始处理
     *
     * @param buffer 要处理的缓冲流
     * */
    internal fun onResponse(buffer: ByteBuffer, request: Request? = null) {
        response = Response().also {
            it.request = request
        }
        index = -1
        process(buffer)
    }

    internal fun onResponseFinished() {
        for (interceptor in interceptors) {
            interceptor.onResponseFinished()
        }
        response = null
    }

    override fun process(buffer: ByteBuffer) {
        index++
        if (index >= interceptors.size) return
        interceptors[index].intercept(this, buffer)
    }

}
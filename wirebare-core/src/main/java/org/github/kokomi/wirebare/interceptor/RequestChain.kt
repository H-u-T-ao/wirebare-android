package org.github.kokomi.wirebare.interceptor

import java.nio.ByteBuffer

/**
 * 请求责任链
 *
 * @param interceptors 请求责任链
 * */
class RequestChain(private val interceptors: List<RequestInterceptor>) : InterceptorChain {

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
        for(interceptor in interceptors) {
            interceptor.onRequestFinished(request)
        }
    }

    override fun process(buffer: ByteBuffer) {
        index++
        if (index >= interceptors.size) return
        interceptors[index].intercept(buffer, this)
    }

}
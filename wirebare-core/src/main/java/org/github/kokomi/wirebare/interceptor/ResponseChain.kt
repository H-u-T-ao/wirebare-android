package org.github.kokomi.wirebare.interceptor

import java.nio.ByteBuffer

/**
 * 响应责任链
 *
 * @param interceptors 响应责任链
 * */
class ResponseChain(private val interceptors: List<ResponseInterceptor>) : InterceptorChain {

    private var index: Int = -1

    internal lateinit var response: Response

    /**
     * 开始处理
     *
     * @param buffer 要处理的缓冲流
     * */
    @Synchronized
    internal fun startProcessing(buffer: ByteBuffer) {
        response = Response()
        index = -1
        process(buffer)
    }

    override fun process(buffer: ByteBuffer) {
        index++
        if (index >= interceptors.size) return
        interceptors[index].intercept(buffer, this)
    }

}
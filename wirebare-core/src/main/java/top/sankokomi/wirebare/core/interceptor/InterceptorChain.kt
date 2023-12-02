package top.sankokomi.wirebare.core.interceptor

import java.nio.ByteBuffer

/**
 * 拦截器责任链
 * */
interface InterceptorChain {

    /**
     * 处理请求体
     * */
    fun processRequestNext(buffer: ByteBuffer)

    /**
     * 请求体处理完毕
     * */
    fun processRequestFinishedNext()

    /**
     * 处理响应体
     * */
    fun processResponseNext(buffer: ByteBuffer)

    /**
     * 响应体处理完毕
     * */
    fun processResponseFinishedNext()

}
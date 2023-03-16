package org.github.kokomi.wirebare.interceptor

import java.nio.ByteBuffer

/**
 * 拦截器责任链
 * */
interface InterceptorChain {

    /**
     * 继续处理责任链中的下一拦截器
     * */
    fun process(buffer: ByteBuffer)

}
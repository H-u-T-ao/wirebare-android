package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * 拦截器责任链
 * */
interface InterceptorChain<SESSION: Session<*>> {

    /**
     * 处理请求体
     * */
    fun processRequestNext(buffer: ByteBuffer, session: SESSION)

    /**
     * 请求体处理完毕
     * */
    fun processRequestFinishedNext(session: SESSION)

    /**
     * 处理响应体
     * */
    fun processResponseNext(buffer: ByteBuffer, session: SESSION)

    /**
     * 响应体处理完毕
     * */
    fun processResponseFinishedNext(session: SESSION)

}
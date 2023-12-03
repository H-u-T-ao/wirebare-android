package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * 拦截器责任链
 * */
interface InterceptorChain {

    /**
     * 处理请求体
     * */
    fun processRequestNext(buffer: ByteBuffer, session: TcpSession)

    /**
     * 请求体处理完毕
     * */
    fun processRequestFinishedNext(session: TcpSession)

    /**
     * 处理响应体
     * */
    fun processResponseNext(buffer: ByteBuffer, session: TcpSession)

    /**
     * 响应体处理完毕
     * */
    fun processResponseFinishedNext(session: TcpSession)

}
package top.sankokomi.wirebare.core.interceptor

import java.nio.ByteBuffer

/**
 * 拦截器
 *
 * @param C 拦截器责任链
 * */
interface Interceptor<C : InterceptorChain> {

    /**
     * 当此拦截器被触发时回调此函数
     *
     * @param buffer 缓冲流，仅包含 TCP 的数据部分
     * @param chain 责任链，执行 [InterceptorChain.process] 继续调度责任链的下一拦截器
     * */
    fun intercept(buffer: ByteBuffer, chain: C)

}
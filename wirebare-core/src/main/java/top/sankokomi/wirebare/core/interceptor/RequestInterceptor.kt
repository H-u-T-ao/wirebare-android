package top.sankokomi.wirebare.core.interceptor

import java.nio.ByteBuffer

/**
 * 请求拦截器，实现 [onRequest] 即可对格式化完成的请求信息以及请求字节流进行操作
 * */
abstract class RequestInterceptor : Interceptor<RequestChain> {

    /**
     * 当接收到请求字节流时回调
     *
     * @param request 格式化完成的请求信息
     * @param buffer 请求字节流
     * */
    abstract fun onRequest(request: Request, buffer: ByteBuffer)

    abstract fun onRequestFinished(request: Request)

    final override fun intercept(buffer: ByteBuffer, chain: RequestChain) {
        onRequest(chain.request, buffer)
        chain.process(buffer)
    }

}
package top.sankokomi.wirebare.core.interceptor.request

import top.sankokomi.wirebare.core.interceptor.Interceptor
import java.nio.ByteBuffer

/**
 * 请求拦截器，实现 [onRequest] 即可对格式化完成的请求信息以及请求字节流进行操作
 * */
open class RequestInterceptor : Interceptor<RequestChain> {

    /**
     * 当接收到请求字节流时回调
     *
     * @param request 格式化完成的请求信息
     * @param buffer 请求字节流
     * */
    open fun onRequest(request: Request, buffer: ByteBuffer) {
    }

    open fun onRequestFinished() {
    }

    final override fun intercept(chain: RequestChain, buffer: ByteBuffer) {
        chain.request?.let { req ->
            onRequest(req, buffer)
            chain.process(buffer)
        }
    }

}
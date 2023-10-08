package top.sankokomi.wirebare.core.interceptor

import java.nio.ByteBuffer

/**
 * 响应拦截器，实现 [onResponse] 即可对格式化完成的响应信息以及响应字节流进行操作
 * */
abstract class ResponseInterceptor : Interceptor<ResponseChain> {

    /**
     * 当接收到响应字节流时回调
     *
     * @param response 格式化完成的响应信息
     * @param buffer 响应字节流
     * */
    abstract fun onResponse(response: Response, buffer: ByteBuffer)

    abstract fun onResponseFinished(response: Response)

    final override fun intercept(buffer: ByteBuffer, chain: ResponseChain) {
        onResponse(chain.response, buffer)
        chain.process(buffer)
    }

}
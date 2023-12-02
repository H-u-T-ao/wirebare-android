package top.sankokomi.wirebare.core.interceptor.response

import top.sankokomi.wirebare.core.interceptor.Interceptor
import java.nio.ByteBuffer

/**
 * 响应拦截器，实现 [onResponse] 即可对格式化完成的响应信息以及响应字节流进行操作
 * */
open class ResponseInterceptor : Interceptor<ResponseChain> {

    /**
     * 当接收到响应字节流时回调
     *
     * @param response 格式化完成的响应信息
     * @param buffer 响应字节流
     * */
    open fun onResponse(response: Response, buffer: ByteBuffer) {
    }

    open fun onResponseFinished() {
    }

    final override fun intercept(chain: ResponseChain, buffer: ByteBuffer) {
        chain.response?.let { rsp ->
            onResponse(rsp, buffer)
            chain.process(buffer)
        }
    }

}
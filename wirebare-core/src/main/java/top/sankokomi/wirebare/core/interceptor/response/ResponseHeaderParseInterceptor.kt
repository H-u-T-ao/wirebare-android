package top.sankokomi.wirebare.core.interceptor.response

import java.nio.ByteBuffer

/**
 * 响应头格式化拦截器，负责格式化 [ResponseChain.response]
 * */
class ResponseHeaderParseInterceptor : ResponseInterceptor() {

    override fun onResponse(response: Response, buffer: ByteBuffer) {
    }

    override fun onResponseFinished(response: Response) {
    }

}
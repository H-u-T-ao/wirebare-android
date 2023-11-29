package top.sankokomi.wirebare.core.interceptor.request

import java.nio.ByteBuffer

/**
 * HTTP 请求 URL 拦截器
 * */
abstract class HttpRequestUrlInterceptor : RequestInterceptor() {

    /**
     * 实现此函数，即可拦截 HTTP 请求的 URL
     *
     * @param url HTTP 请求的 URL
     * */
    abstract fun onRequest(url: String?)

    final override fun onRequest(request: Request, buffer: ByteBuffer) {
        if (request.path?.isBlank() != false) return
        onRequest(request.url)
    }

}
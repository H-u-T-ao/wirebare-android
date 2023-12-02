package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import java.nio.ByteBuffer

class HttpInterceptChain(
    private val interceptors: List<HttpInterceptor>
) : InterceptorChain {

    var request: Request = Request()
        private set

    var response: Response = Response()
        private set

    private var interceptorIndex = -1

    override fun processRequestNext(buffer: ByteBuffer) {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onRequest(this, buffer)
    }

    override fun processRequestFinishedNext() {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onRequestFinished(this) ?: let {
            request = Request()
        }
    }

    override fun processResponseNext(buffer: ByteBuffer) {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onResponse(this, buffer)
    }

    override fun processResponseFinishedNext() {
        interceptorIndex++
        interceptors.getOrNull(interceptorIndex)?.onResponseFinished(this) ?: let {
            response = Response()
        }
    }

    internal fun processRequest(buffer: ByteBuffer) {
        interceptorIndex = -1
        processRequestNext(buffer)
    }

    internal fun processRequestFinished() {
        interceptorIndex = -1
        processRequestFinishedNext()
    }

    internal fun processResponse(buffer: ByteBuffer) {
        interceptorIndex = -1
        processResponseNext(buffer)
    }

    internal fun processResponseFinished() {
        interceptorIndex = -1
        processResponseFinishedNext()
    }

}
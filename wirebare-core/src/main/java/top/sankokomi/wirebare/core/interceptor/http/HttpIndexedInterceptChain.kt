package top.sankokomi.wirebare.core.interceptor.http

import java.nio.ByteBuffer

abstract class HttpIndexedInterceptChain : HttpInterceptor {

    private var requestIndex = -1

    private var responseIndex = -1

    open fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, index: Int) {
        chain.processRequestNext(buffer)
    }

    open fun onRequestFinished(chain: HttpInterceptChain, index: Int) {
        chain.processRequestFinishedNext()
    }

    open fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, index: Int) {
        chain.processResponseNext(buffer)
    }

    open fun onResponseFinished(chain: HttpInterceptChain, index: Int) {
        chain.processResponseFinishedNext()
    }

    final override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer) {
        requestIndex++
        onRequest(chain, buffer, requestIndex)
    }

    final override fun onRequestFinished(chain: HttpInterceptChain) {
        onRequestFinished(chain, requestIndex)
        requestIndex = -1
    }

    final override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer) {
        responseIndex++
        onResponse(chain, buffer, responseIndex)
    }

    final override fun onResponseFinished(chain: HttpInterceptChain) {
        onResponseFinished(chain, responseIndex)
        responseIndex = -1
    }

}
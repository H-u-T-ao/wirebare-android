package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.Interceptor
import java.nio.ByteBuffer

interface HttpInterceptor : Interceptor<HttpInterceptChain> {

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer) {
        chain.processRequestNext(buffer)
    }

    override fun onRequestFinished(chain: HttpInterceptChain) {
        chain.processRequestFinishedNext()
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer) {
        chain.processResponseNext(buffer)
    }

    override fun onResponseFinished(chain: HttpInterceptChain) {
        chain.processResponseFinishedNext()
    }

}
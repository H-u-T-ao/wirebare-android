package top.sankokomi.wirebare.ui.wireinfo

import top.sankokomi.wirebare.core.interceptor.http.AsyncHttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.AsyncHttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.AsyncHttpInterceptor
import top.sankokomi.wirebare.core.interceptor.http.AsyncHttpInterceptorFactory
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.ui.record.HttpRecorder
import java.nio.ByteBuffer

class WireBareHttpInterceptor(
    private val onRequest: (HttpRequest) -> Unit,
    private val onResponse: (HttpResponse) -> Unit
) : AsyncHttpIndexedInterceptor() {

    class Factory(
        private val onRequest: (HttpRequest) -> Unit,
        private val onResponse: (HttpResponse) -> Unit
    ) : AsyncHttpInterceptorFactory {
        override fun create(): AsyncHttpInterceptor {
            return WireBareHttpInterceptor(onRequest, onResponse)
        }
    }

    override suspend fun onRequest(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        if (index == 0) {
            onRequest(session.request)
        }
        HttpRecorder.addRequestRecord(session.request, buffer)
        super.onRequest(chain, buffer, session, index)
    }

    override suspend fun onRequestFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession,
        index: Int
    ) {
        HttpRecorder.addRequestRecord(session.request, null)
        super.onRequestFinished(chain, session, index)
    }

    override suspend fun onResponse(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        if (index == 0) {
            onResponse(session.response)
        }
        HttpRecorder.addResponseRecord(session.response, buffer)
        super.onResponse(chain, buffer, session, index)
    }

    override suspend fun onResponseFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession,
        index: Int
    ) {
        HttpRecorder.addResponseRecord(session.response, null)
        super.onResponseFinished(chain, session, index)
    }
}
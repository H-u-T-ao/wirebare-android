package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.ssl.RequestSSLCodec
import top.sankokomi.wirebare.core.ssl.ResponseSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.ssl.SSLEngineFactory
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.mergeBuffer
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class HttpSSLCodecInterceptor(
    configuration: WireBareConfiguration
): HttpInterceptor, HttpSSLRefluxReceiver {

    private val factory = SSLEngineFactory(configuration)

    private val requestCodec = RequestSSLCodec(factory)

    private val responseCodec = ResponseSSLCodec(factory)

    private val pendingReqCiphertext = ConcurrentLinkedQueue<ByteBuffer>()

    private val pendingRspCiphertext = ConcurrentLinkedQueue<ByteBuffer>()

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        pendingReqCiphertext.add(buffer)
        requestCodec.decode(
            session,
            pendingReqCiphertext.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingReqCiphertext.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    chain.processRequestFinial(buffer, target)
                }

                override fun decryptSuccess(target: ByteBuffer) {
                    chain.processRequestNext(target, session)
                }

                override fun encryptSuccess(target: ByteBuffer) {
                    chain.skipRequestAndReflux(target)
                }
            }
        )
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        pendingRspCiphertext.add(buffer)
        responseCodec.decode(
            session,
            pendingRspCiphertext.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingRspCiphertext.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    chain.processRequestFinial(buffer, target)
                }

                override fun decryptSuccess(target: ByteBuffer) {
                    chain.processResponseNext(target, session)
                }

                override fun encryptSuccess(target: ByteBuffer) {
                    chain.skipResponseAndReflux(target)
                }
            }
        )
    }

    override fun onRequestReflux(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession
    ) {
        responseCodec.encode(
            session,
            buffer,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
                    chain.processRequestFinial(buffer, target)
                }
            }
        )
    }

    override fun onResponseReflux(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession
    ) {
        requestCodec.encode(
            session,
            buffer,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
                    chain.processResponseFinial(buffer, target)
                }
            }
        )
    }
}
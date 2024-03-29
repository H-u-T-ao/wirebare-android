package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.ssl.RequestSSLCodec
import top.sankokomi.wirebare.core.ssl.ResponseSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.ssl.SSLEngineFactory
import top.sankokomi.wirebare.core.util.mergeBuffer
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

class HttpSSLCodecInterceptor(
    configuration: WireBareConfiguration
) : HttpInterceptor, HttpSSLRefluxReceiver {

    private val factory = SSLEngineFactory(configuration)

    private val requestCodec = RequestSSLCodec(factory)

    private val responseCodec = ResponseSSLCodec(factory)

    private val pendingReqCiphertext = ConcurrentLinkedQueue<ByteBuffer>()

    private val pendingRspCiphertext = ConcurrentLinkedQueue<ByteBuffer>()

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        val request = chain.curReqRsp(session)?.first ?: return
        if (request.isHttps != true) {
            super.onRequest(chain, buffer, session)
            return
        }
        val host = request.hostInternal ?: return
        chain.skipOriginBuffer()
        val pendingBuffer = LinkedBlockingQueue<ByteBuffer>()
        responseCodec.handshakeIfNecessary(
            session,
            host,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
//                    chain.processExtraBuffer(target, BufferDirection.RemoteServer)
                    pendingBuffer.offer(target)
                }
            }
        )
        chain.processExtraBuffer(
            pendingBuffer.mergeBuffer(),
            BufferDirection.RemoteServer
        )
        pendingReqCiphertext.add(buffer)
        var direction: BufferDirection? = null
        requestCodec.decode(
            session,
            host,
            pendingReqCiphertext.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingReqCiphertext.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    chain.processRequestFinial(target)
                }

                override fun decryptSuccess(target: ByteBuffer) {
//                    chain.processRequestNext(target, session)
                    pendingBuffer.offer(target)
                }

                override fun encryptSuccess(target: ByteBuffer) {
//                    chain.processExtraBuffer(target, BufferDirection.ProxyClient)
                    pendingBuffer.offer(target)
                    direction = BufferDirection.ProxyClient
                }
            }
        )
        if (direction != null && pendingBuffer.isNotEmpty()) {
            chain.processExtraBuffer(
                pendingBuffer.mergeBuffer(false),
                direction!!
            )
        } else if (pendingBuffer.isNotEmpty()) {
            chain.processRequestNext(
                pendingBuffer.mergeBuffer(false),
                session
            )
        }
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        val response = chain.curReqRsp(session)?.second ?: return
        if (response.isHttps != true) {
            super.onResponse(chain, buffer, session)
            return
        }
        val host = chain.curReqRsp(session)?.second?.hostInternal ?: return
        chain.skipOriginBuffer()
        pendingRspCiphertext.add(buffer)
        val pendingBuffer = LinkedBlockingQueue<ByteBuffer>()
        var direction: BufferDirection? = null
        responseCodec.decode(
            session,
            host,
            pendingRspCiphertext.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingRspCiphertext.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    chain.processResponseFinial(target)
                }

                override fun decryptSuccess(target: ByteBuffer) {
//                    chain.processResponseNext(target, session)
                    pendingBuffer.offer(target)
                }

                override fun encryptSuccess(target: ByteBuffer) {
//                    chain.processExtraBuffer(target, BufferDirection.RemoteServer)
                    pendingBuffer.offer(target)
                    direction = BufferDirection.RemoteServer
                }
            }
        )
        if (direction != null && pendingBuffer.isNotEmpty()) {
            chain.processExtraBuffer(
                pendingBuffer.mergeBuffer(false),
                direction!!
            )
        } else if (pendingBuffer.isNotEmpty()) {
            chain.processResponseNext(
                pendingBuffer.mergeBuffer(false),
                session
            )
        }
    }

    override fun onRequestReflux(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession
    ) {
        val host = chain.curReqRsp(session)?.first?.hostInternal ?: return
        val pendingBuffer = LinkedBlockingQueue<ByteBuffer>()
        responseCodec.encode(
            session,
            host,
            buffer,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
//                    chain.processExtraBuffer(target, BufferDirection.RemoteServer)
                    pendingBuffer.offer(target)
                }
            }
        )
        if (pendingBuffer.isNotEmpty()) {
            chain.processExtraBuffer(
                pendingBuffer.mergeBuffer(false),
                BufferDirection.RemoteServer
            )
        }
    }

    override fun onResponseReflux(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession
    ) {
        val host = chain.curReqRsp(session)?.second?.hostInternal ?: return
        val pendingBuffer = LinkedBlockingQueue<ByteBuffer>()
        requestCodec.encode(
            session,
            host,
            buffer,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
//                    chain.processExtraBuffer(target, BufferDirection.ProxyClient)
                    pendingBuffer.offer(target)
                }
            }
        )
        if (pendingBuffer.isNotEmpty()) {
            chain.processExtraBuffer(
                pendingBuffer.mergeBuffer(false),
                BufferDirection.ProxyClient
            )
        }
    }
}
package top.sankokomi.wirebare.core.interceptor.netty.ssl

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import top.sankokomi.wirebare.core.interceptor.netty.NettyWireContext
import top.sankokomi.wirebare.core.interceptor.netty.NettyWirePacket
import top.sankokomi.wirebare.core.ssl.JKS
import top.sankokomi.wirebare.core.ssl.RequestSSLCodec
import top.sankokomi.wirebare.core.ssl.ResponseSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.ssl.SSLEngineFactory
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.mergeBuffer
import top.sankokomi.wirebare.core.util.toByteBuf
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

internal class NettyHttpSSLCodec(jks: JKS) :
    MessageToMessageCodec<NettyWireContext, NettyWireContext>() {

    private val factory = SSLEngineFactory(jks)

    internal val requestCodec = RequestSSLCodec(factory)

    internal val responseCodec = ResponseSSLCodec(factory)

    private val pendingReqCiphertextQueue = LinkedBlockingQueue<ByteBuffer>()

    private val pendingRspCiphertextQueue = LinkedBlockingQueue<ByteBuffer>()

    override fun decode(ctx: ChannelHandlerContext, msg: NettyWireContext, out: MutableList<Any>) {
        val (buffer, session, client) = msg
        val (request, _, tcpSession) = session
        if (request.isHttps != true) {
            out += msg
            return
        }
        val host = request.hostInternal ?: return
        responseCodec.handshakeIfNecessary(
            tcpSession,
            host,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
                    client.writeAndFlush(target.toByteBuf())
                }
            }
        )
        pendingReqCiphertextQueue.add(buffer)
        requestCodec.decode(
            tcpSession,
            host,
            pendingReqCiphertextQueue.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingReqCiphertextQueue.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    WireBareLogger.warn("SSL 引擎创建失败")
                }

                override fun decryptSuccess(target: ByteBuffer) {
                    session.request.isPlaintext = true
                    out += NettyWireContext(target, session, client)
                }

                override fun encryptSuccess(target: ByteBuffer) {
                    ctx.channel().writeAndFlush(
                        NettyWirePacket(target, true)
                    )
                }
            }
        )
    }

    override fun encode(ctx: ChannelHandlerContext, msg: NettyWireContext, out: MutableList<Any>) {
        val (buffer, session, client) = msg
        val (_, response, tcpSession) = session
        if (response.isHttps != true) {
            out += msg
            return
        }
        val host = response.hostInternal ?: return
        pendingRspCiphertextQueue.add(buffer)
        responseCodec.decode(
            tcpSession,
            host,
            pendingRspCiphertextQueue.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingRspCiphertextQueue.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    WireBareLogger.warn("SSL 引擎创建失败")
                }

                override fun decryptSuccess(target: ByteBuffer) {
                    session.response.isPlaintext = true
                    out += NettyWireContext(target, session, client)
                }

                override fun encryptSuccess(target: ByteBuffer) {
                    client.writeAndFlush(target.toByteBuf())
                }
            }
        )
    }
}
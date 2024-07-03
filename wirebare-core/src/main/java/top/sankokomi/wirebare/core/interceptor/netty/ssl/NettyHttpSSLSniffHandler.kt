package top.sankokomi.wirebare.core.interceptor.netty.ssl

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import top.sankokomi.wirebare.core.interceptor.netty.NettyWireContext
import top.sankokomi.wirebare.core.ssl.judgeIsHttps
import top.sankokomi.wirebare.core.util.newString
import top.sankokomi.wirebare.core.util.readShort
import java.nio.ByteBuffer
import java.util.Locale

internal class NettyHttpSSLSniffHandler : ChannelDuplexHandler() {

    private var encodeIndex = -1
    private var decodeIndex = -1

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is NettyWireContext) {
            super.channelRead(ctx, msg)
            return
        }
        val (buffer, session) = msg
        if (++decodeIndex == 0) {
            val (request, _) = session
            request.isHttps = buffer.judgeIsHttps
            session.request.isPlaintext = request.isHttps == false
            request.hostInternal = ensureHost(request.isHttps, buffer)
        }
        super.channelRead(ctx, msg)
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise?) {
        if (msg !is NettyWireContext) {
            super.write(ctx, msg, promise)
            return
        }
        val (_, session) = msg
        if (++encodeIndex == 0) {
            val (request, response) = session
            response.isHttps = request.isHttps
            session.response.isPlaintext = request.isHttps == false
            response.hostInternal = request.hostInternal
        }
        super.write(ctx, msg, promise)
    }

    private fun ensureHost(isHttps: Boolean?, buffer: ByteBuffer): String? {
        return when (isHttps) {
            true -> {
                parseHttpsHost(buffer, buffer.position(), buffer.remaining())
            }

            false -> {
                parseHttpHost(buffer, buffer.position(), buffer.remaining())
            }

            else -> {
                null
            }
        }
    }

    private fun parseHttpHost(buffer: ByteBuffer, start: Int, size: Int): String? {
        val header = buffer.newString(start, size)
        val headers = header.split("\r\n").dropLastWhile {
            it.isEmpty()
        }.toTypedArray()
        if (headers.size <= 1) {
            return null
        }
        for (i in 1 until headers.size) {
            val requestHeader = headers[i]
            if (requestHeader.isEmpty()) {
                return null
            }
            val nameValue = requestHeader.split(":").dropLastWhile {
                it.isEmpty()
            }.toTypedArray()
            if (nameValue.size < 2) {
                return null
            }
            val name = nameValue[0].trim { it <= ' ' }
            val value = requestHeader.replaceFirst(
                "${nameValue[0]}: ", ""
            ).trim { it <= ' ' }
            if (name.lowercase(Locale.getDefault()) == "host") {
                return value
            }
        }
        return null
    }

    /**
     * # 头部需要跳过以下固定的 43 字节：
     * ## 记录层（5字节）
     * - 消息类型（1字节），对于握手，则固定为 0x16
     * - TLS 版本（2字节），对于 TLSv1.2，则固定为 0x0303
     * - 长度（2字节）
     *
     * ## 握手层（38字节）
     * - 握手类型（2字节），对于 Client Hello，则固定为 0x0001
     * - 长度（2字节）
     * - TLS 版本（2字节）
     * - 随机数（32字节）
     * */
    private fun parseHttpsHost(
        buffer: ByteBuffer,
        start: Int,
        size: Int
    ): String? {
        if (size <= 43) {
            return null
        }
        val array = buffer.array()
        var pointer = start
        val limit = pointer + size
        // 0x16 即十进制 22 ，表示当前是 SSL/TLS 握手
        if (array[pointer].toInt() != 0x16) {
            return null
        }
        // 跳过固定的 43 字节
        pointer += 43

        // 跳过 Session ID
        if (pointer + 1 > limit) {
            return null
        }
        val sessionIDLength = array[pointer++].toInt() and 0xFF
        pointer += sessionIDLength

        // 跳过加密套件 Cipher Suites
        if (pointer + 2 > limit) {
            return null
        }
        val cipherSuitesLength = array.readShort(pointer).toInt() and 0xFFFF
        pointer += 2
        pointer += cipherSuitesLength

        // 跳过压缩方法 Compression Method
        if (pointer + 1 > limit) {
            return null
        }
        val compressionMethodLength = array[pointer++].toInt() and 0xFF
        pointer += compressionMethodLength

        // 拓展 Extensions
        if (pointer + 2 > limit) {
            return null
        }
        val extensionsLength = array.readShort(pointer).toInt() and 0xFFFF
        pointer += 2
        if (pointer + extensionsLength > limit) {
            return null
        }
        while (pointer + 4 <= limit) {
            val type = array.readShort(pointer).toInt() and 0xFFFF
            pointer += 2
            var length = array.readShort(pointer).toInt() and 0xFFFF
            pointer += 2
            // Server Name Indication
            // SNI 拓展的类型代码是 0x00
            if (type == 0x00 && length > 5) {
                pointer += 5
                length -= 5
                return if (pointer + length <= limit) {
                    buffer.newString(pointer, length)
                } else {
                    null
                }
            } else {
                pointer += length
            }
        }
        return null
    }
}
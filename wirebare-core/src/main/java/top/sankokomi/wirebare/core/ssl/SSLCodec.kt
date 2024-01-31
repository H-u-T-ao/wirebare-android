package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.util.readUnsignedByte
import top.sankokomi.wirebare.core.util.readUnsignedShort
import java.nio.ByteBuffer

abstract class SSLCodec {

    abstract fun createSSLEngineWrapper(session: TcpSession, host: String): WireBareSSLEngine?

    internal fun decode(
        session: TcpSession,
        host: String,
        buffer: ByteBuffer,
        callback: SSLCallback
    ) {
        when (verifyPacket(buffer)) {
            VerifyResult.NotEncrypted -> {
                callback.decryptSuccess(buffer)
            }

            VerifyResult.NotEnough -> {
                callback.shouldPending(buffer)
            }

            VerifyResult.Ready -> {
                realDecode(session, host, buffer, callback)
            }
        }
    }

    internal fun encode(
        session: TcpSession,
        host: String,
        buffer: ByteBuffer,
        callback: SSLCallback
    ) {
        realEncode(session, host, buffer, callback)
    }

    private fun realDecode(
        session: TcpSession,
        host: String,
        buffer: ByteBuffer,
        callback: SSLCallback
    ) {
        val engine = createSSLEngineWrapper(session, host)
        if (engine == null) {
            callback.sslFailed(buffer)
            return
        }
        engine.decodeBuffer(buffer, callback)
    }

    private fun realEncode(
        session: TcpSession,
        host: String,
        buffer: ByteBuffer,
        callback: SSLCallback
    ) {
        val engine = createSSLEngineWrapper(session, host)
        if (engine == null) {
            // 按理来说这里应该不可能进来
            callback.sslFailed(buffer)
            return
        }
        engine.encodeBuffer(buffer, callback)
    }

    enum class VerifyResult {
        /**
         * 数据未完整，等待下一个数据包再做解密
         * */
        NotEnough,

        /**
         * 数据包是明文，不需要解密
         * */
        NotEncrypted,

        /**
         * 数据包准备就绪，可以进行解密
         * */
        Ready
    }

    private fun verifyPacket(buffer: ByteBuffer): VerifyResult {
        val position = buffer.position()
        if (buffer.remaining() < SSLPredicate.SSL_RECORD_HEADER_LENGTH) {
            return VerifyResult.NotEnough
        }
        var packetLength = 0
        var tls = when (buffer.readUnsignedByte(position)) {
            SSLPredicate.SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC,
            SSLPredicate.SSL_CONTENT_TYPE_ALERT,
            SSLPredicate.SSL_CONTENT_TYPE_HANDSHAKE,
            SSLPredicate.SSL_CONTENT_TYPE_APPLICATION_DATA,
            SSLPredicate.SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT -> true

            else -> false
        }
        if (tls) {
            val majorVersion = buffer.readUnsignedByte(position + 1)
            if (majorVersion == 3) {
                packetLength = buffer.readUnsignedShort(
                    position + 3
                ) + SSLPredicate.SSL_RECORD_HEADER_LENGTH
                if (packetLength <= SSLPredicate.SSL_RECORD_HEADER_LENGTH) {
                    tls = false
                }
            } else {
                tls = false
            }
        }
        if (!tls) {
            val headerLength = if (
                buffer.readUnsignedByte(
                    position
                ) and 0x80 != 0
            ) 2 else 3
            val majorVersion: Int = buffer.readUnsignedByte(position + headerLength + 1)
            if (majorVersion == 2 || majorVersion == 3) {
                packetLength = if (headerLength == 2) {
                    (buffer.getShort(position).toInt() and 0x7FFF) + 2
                } else {
                    (buffer.getShort(position).toInt() and 0x3FFF) + 3
                }
                if (packetLength <= headerLength) {
                    return VerifyResult.NotEnough
                }
            } else {
                return VerifyResult.NotEncrypted
            }
        }
        if (packetLength > buffer.remaining()) {
            return VerifyResult.NotEnough
        }
        return VerifyResult.Ready
    }

}
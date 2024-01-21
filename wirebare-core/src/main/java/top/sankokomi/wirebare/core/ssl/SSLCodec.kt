package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.util.readUnsignedByte
import top.sankokomi.wirebare.core.util.readUnsignedShort
import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine

abstract class SSLCodec {

    companion object {
        private const val SSL_RECORD_HEADER_LENGTH = 5

        private const val SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20
        private const val SSL_CONTENT_TYPE_ALERT = 21
        private const val SSL_CONTENT_TYPE_HANDSHAKE = 22
        private const val SSL_CONTENT_TYPE_APPLICATION_DATA = 23
        private const val SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24
    }

    abstract fun createSSLEngineWrapper(session: TcpSession): WireBareSSLEngine?

    internal fun decode(
        session: TcpSession,
        buffer: ByteBuffer,
        callback: DecodeCallback
    ) {
        val result = verifyPacket(buffer)
        when (result) {
            VerifyResult.NotEncrypted -> {
                callback.decodeSuccess(buffer)
            }

            VerifyResult.NotEnough -> {
                callback.shouldPending(buffer)
            }

            VerifyResult.Ready -> {
                realDecode(session, buffer, callback)
            }
        }
    }

    private fun realDecode(
        session: TcpSession,
        buffer: ByteBuffer,
        callback: DecodeCallback
    ) {
        val engine = createSSLEngineWrapper(session)
        if (engine == null) {
            callback.decodeFailed(buffer)
            return
        }

    }

    private fun handshake() {

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
        if (buffer.remaining() < SSL_RECORD_HEADER_LENGTH) {
            return VerifyResult.NotEnough
        }
        var packetLength = 0
        var tls = when (buffer.readUnsignedByte(position)) {
            SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC,
            SSL_CONTENT_TYPE_ALERT,
            SSL_CONTENT_TYPE_HANDSHAKE,
            SSL_CONTENT_TYPE_APPLICATION_DATA,
            SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT -> true

            else -> false
        }
        if (tls) {
            val majorVersion = buffer.readUnsignedByte(position + 1)
            if (majorVersion == 3) {
                packetLength = buffer.readUnsignedShort(
                    position + 3
                ) + SSL_RECORD_HEADER_LENGTH
                if (packetLength <= SSL_RECORD_HEADER_LENGTH) {
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
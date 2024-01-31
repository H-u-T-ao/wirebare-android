package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.ssl.judgeIsHttps
import top.sankokomi.wirebare.core.util.readShort
import java.nio.ByteBuffer
import java.util.Locale

class HttpSSLSniffInterceptor : HttpIndexedInterceptor() {
    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        index: Int
    ) {
        if (index == 0) {
            val (request, _) = chain.curReqRsp(session) ?: return
            request.isHttps = buffer.judgeIsHttps
            request.hostInternal = ensureHost(request.isHttps, buffer)
        }
        super.onRequest(chain, buffer, session, index)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        index: Int
    ) {
        if (index == 0) {
            val (request, response) = chain.curReqRsp(session) ?: return
            response.isHttps = request.isHttps
            response.hostInternal = request.hostInternal
        }
        super.onResponse(chain, buffer, session, index)
    }

    private fun ensureHost(isHttps: Boolean?, buffer: ByteBuffer): String? {
        return when (isHttps) {
            true -> {
                parseHttpsHost(buffer.array(), buffer.position(), buffer.remaining())
            }

            false -> {
                parseHttpHost(buffer.array(), buffer.position(), buffer.remaining())
            }

            else -> {
                null
            }
        }
    }

    private fun parseHttpHost(buffer: ByteArray, offset: Int, size: Int): String? {
        val header = String(buffer, offset, size)
        val headers = header.split("\\r\\n".toRegex()).dropLastWhile {
            it.isEmpty()
        }.toTypedArray()
        if (headers.size <= 1) {
            return null
        }
        for (i in 1 until headers.size) {
            val requestHeader = headers[i]
            // Reach the header end
            if (requestHeader.isEmpty()) {
                return null
            }
            val nameValue = requestHeader.split(":".toRegex()).dropLastWhile {
                it.isEmpty()
            }.toTypedArray()
            if (nameValue.size < 2) {
                return null
            }
            val name = nameValue[0].trim { it <= ' ' }
            val value =
                requestHeader.replaceFirst((nameValue[0] + ": ").toRegex(), "").trim { it <= ' ' }
            if (name.lowercase(Locale.getDefault()) == "host") {
                return value
            }
        }
        return null
    }

    private fun parseHttpsHost(buffer: ByteArray, offset: Int, size: Int): String? {
        var offset = offset
        val limit = offset + size
        // Client Hello
        if (size <= 43 || buffer[offset].toInt() != 0x16) {
            return null
        }
        // Skip 43 byte header
        offset += 43

        // Read sessionID
        if (offset + 1 > limit) {
            return null
        }
        val sessionIDLength = buffer[offset++].toInt() and 0xFF
        offset += sessionIDLength

        // Read cipher suites
        if (offset + 2 > limit) {
            return null
        }
        val cipherSuitesLength: Int = buffer.readShort(offset).toInt() and 0xFFFF
        offset += 2
        offset += cipherSuitesLength

        // Read Compression method.
        if (offset + 1 > limit) {
            return null
        }
        val compressionMethodLength = buffer[offset++].toInt() and 0xFF
        offset += compressionMethodLength

        // Read Extensions
        if (offset + 2 > limit) {
            return null
        }
        val extensionsLength: Int = buffer.readShort(offset).toInt() and 0xFFFF
        offset += 2
        if (offset + extensionsLength > limit) {
            return null
        }
        while (offset + 4 <= limit) {
            val type0 = buffer[offset++].toInt() and 0xFF
            val type1 = buffer[offset++].toInt() and 0xFF
            var length: Int = buffer.readShort(offset).toInt() and 0xFFFF
            offset += 2
            // Got the SNI info
            if (type0 == 0x00 && type1 == 0x00 && length > 5) {
                offset += 5
                length -= 5
                return if (offset + length > limit) {
                    null
                } else String(buffer, offset, length)
            } else {
                offset += length
            }
        }
        return null
    }

}
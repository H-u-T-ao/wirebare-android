package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.ssl.judgeIsHttps
import top.sankokomi.wirebare.core.util.readShort
import java.nio.ByteBuffer
import java.util.Locale

class HttpSSLSniffInterceptor : HttpIndexedInterceptor() {
    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0) {
            val (request, _) = session
            request.isHttps = buffer.judgeIsHttps
            session.isPlaintext = request.isHttps == false
            request.hostInternal = ensureHost(request.isHttps, buffer)
        }
        super.onRequest(chain, buffer, session, tunnel, index)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0) {
            val (request, response) = session
            response.isHttps = request.isHttps
            session.isPlaintext = request.isHttps == false
            response.hostInternal = request.hostInternal
        }
        super.onResponse(chain, buffer, session, tunnel, index)
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
        val headers = header.split("\r\n").dropLastWhile {
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

    private fun parseHttpsHost(buffer: ByteArray, offset: Int, size: Int): String? {
        var o = offset
        val limit = o + size
        // Client Hello
        if (size <= 43 || buffer[o].toInt() != 0x16) {
            return null
        }
        // Skip 43 byte header
        o += 43

        // Read sessionID
        if (o + 1 > limit) {
            return null
        }
        val sessionIDLength = buffer[o++].toInt() and 0xFF
        o += sessionIDLength

        // Read cipher suites
        if (o + 2 > limit) {
            return null
        }
        val cipherSuitesLength: Int = buffer.readShort(o).toInt() and 0xFFFF
        o += 2
        o += cipherSuitesLength

        // Read Compression method.
        if (o + 1 > limit) {
            return null
        }
        val compressionMethodLength = buffer[o++].toInt() and 0xFF
        o += compressionMethodLength

        // Read Extensions
        if (o + 2 > limit) {
            return null
        }
        val extensionsLength: Int = buffer.readShort(o).toInt() and 0xFFFF
        o += 2
        if (o + extensionsLength > limit) {
            return null
        }
        while (o + 4 <= limit) {
            val type0 = buffer[o++].toInt() and 0xFF
            val type1 = buffer[o++].toInt() and 0xFF
            var length: Int = buffer.readShort(o).toInt() and 0xFFFF
            o += 2
            // Got the SNI info
            if (type0 == 0x00 && type1 == 0x00 && length > 5) {
                o += 5
                length -= 5
                return if (o + length > limit) {
                    null
                } else String(buffer, o, length)
            } else {
                o += length
            }
        }
        return null
    }

}
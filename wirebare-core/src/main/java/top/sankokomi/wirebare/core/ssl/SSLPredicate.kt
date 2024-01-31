package top.sankokomi.wirebare.core.ssl

import java.nio.ByteBuffer

object SSLPredicate {
    internal const val SSL_RECORD_HEADER_LENGTH = 5

    internal const val HTTP_METHOD_GET = 'G'.code
    internal const val HTTP_METHOD_HEAD = 'H'.code
    internal const val HTTP_METHOD_POST_PUT_PATCH = 'P'.code
    internal const val HTTP_METHOD_DELETE = 'D'.code
    internal const val HTTP_METHOD_OPTIONS = 'O'.code
    internal const val HTTP_METHOD_TRACE = 'T'.code
    internal const val HTTP_METHOD_CONNECT = 'C'.code

    internal const val SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20
    internal const val SSL_CONTENT_TYPE_ALERT = 21
    internal const val SSL_CONTENT_TYPE_HANDSHAKE = 22
    internal const val SSL_CONTENT_TYPE_APPLICATION_DATA = 23
    internal const val SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24
}

internal val ByteBuffer.judgeIsHttps: Boolean?
    get() {
        if (!hasRemaining()) return null
        return when (get(position()).toInt()) {
            SSLPredicate.HTTP_METHOD_GET,/* GET */
            SSLPredicate.HTTP_METHOD_HEAD,/* HEAD */
            SSLPredicate.HTTP_METHOD_POST_PUT_PATCH,/* POST, PUT, PATCH */
            SSLPredicate.HTTP_METHOD_DELETE,/* DELETE */
            SSLPredicate.HTTP_METHOD_OPTIONS,/* OPTIONS */
            SSLPredicate.HTTP_METHOD_TRACE,/* TRACE */
            SSLPredicate.HTTP_METHOD_CONNECT/* CONNECT */ -> false

            /* HTTPS */
            SSLPredicate.SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC,
            SSLPredicate.SSL_CONTENT_TYPE_ALERT,
            SSLPredicate.SSL_CONTENT_TYPE_HANDSHAKE,
            SSLPredicate.SSL_CONTENT_TYPE_APPLICATION_DATA,
            SSLPredicate.SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT -> true

            else -> null
        }
    }
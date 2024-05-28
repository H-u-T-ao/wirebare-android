@file:Suppress("PropertyName")

package top.sankokomi.wirebare.core.interceptor.http

import java.io.Serializable

/**
 * 请求信息
 * */
class HttpRequest internal constructor() : Serializable {

    var requestTime: Long? = null
        internal set

    /**
     * 来源端口号
     * */
    var sourcePort: Short? = null
        internal set

    var sourcePkgName: String? = null
        internal set

    /**
     * 目的地址
     * */
    var destinationAddress: String? = null
        internal set

    /**
     * 目的端口号
     * */
    var destinationPort: Short? = null
        internal set

    /**
     * 请求的方法，需要是 HTTP/HTTPS 协议才可以解析
     * */
    var method: String? = null
        internal set

    /**
     * true 表示当前请求为 HTTP 请求
     *
     * false 表示当前请求为 HTTPS 请求
     *
     * null 表示未知，既不是 HTTP 也不是 HTTPS
     * */
    var isHttps: Boolean? = null
        internal set

    /**
     * 若为 HTTP/HTTPS 请求，则为 HTTP 版本，否则为 null
     * */
    var httpVersion: String? = null
        internal set

    /**
     * [isHttps] == true 时该值才有效
     *
     * true 表示已经完成 SSL/TLS 的握手流程，拦截器中拿到的都是明文
     * */
    internal var isPlaintext: Boolean? = null

    internal var hostInternal: String? = null

    /**
     * 请求的域名
     * */
    var host: String? = null
        internal set

    /**
     * 请求的路径
     * */
    var path: String? = null
        internal set

    /**
     * 原始的请求头，包含的是最原始的请求头信息
     * */
    var originHead: String? = null
        internal set

    /**
     * 整个请求头，已经以 \r\n 为间隔分隔好
     * */
    var formatHead: List<String>? = null
        internal set

    /**
     * 请求的 URL ，需要是 HTTP/HTTPS 协议才可以解析
     * */
    val url: String?
        get() {
            if (host == null || path == null) {
                return null
            }
            return when (isHttps) {
                false -> "http://${host}${path}"
                true -> "https://${host}${path}"
                else -> null
            }
        }

}

@file:Suppress("PropertyName")

package top.sankokomi.wirebare.core.interceptor.request

/**
 * 请求信息
 * */
class Request internal constructor() {

    companion object {
        const val UNKNOWN_METHOD = "%unknown_method"
        const val UNKNOWN_HTTP_VERSION = "%unknown_http_version%"
        const val UNKNOWN_HOST = "%unknown_host%"
        const val UNKNOWN_PATH = "%unknown_path%"
        const val UNKNOWN_PROTOCOL = "%unknown_protocol%"
    }

    /**
     * 请求的方法，需要是 HTTP 协议才可以解析
     * */
    val method: String get() = _method ?: UNKNOWN_METHOD

    /**
     * true 表示当前请求为 HTTP 请求，false 表示当前请求为 HTTPS 请求，null 表示未知协议
     * */
    val isHttp: Boolean? get() = _isHttp

    /**
     * 若为 HTTP 请求，则为 HTTP 版本，否则为 null
     * */
    val httpVersion: String get() = _httpVersion ?: UNKNOWN_HTTP_VERSION

    /**
     * 请求的域名，需要是 HTTP 协议才可以解析
     * */
    val host: String get() = _host ?: UNKNOWN_HOST

    /**
     * 请求的路径，需要是 HTTP 协议才可以解析
     * */
    val path: String get() = _path ?: UNKNOWN_PATH

    /**
     * 请求的 URL ，需要是 HTTP 协议才可以解析
     * */
    val url: String
        get() = when (isHttp) {
            true -> {
                "http://$host$path"
            }

            false -> {
                "https://$host$path"
            }

            else -> {
                UNKNOWN_PROTOCOL
            }
        }

    internal var _method: String? = null

    internal var _isHttp: Boolean? = null

    internal var _httpVersion: String? = null

    internal var _host: String? = null

    internal var _path: String? = null

}

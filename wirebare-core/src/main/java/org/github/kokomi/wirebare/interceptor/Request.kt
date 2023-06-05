package org.github.kokomi.wirebare.interceptor

/**
 * 请求信息
 * */
class Request internal constructor() {

    internal companion object {
        internal const val UnknownHttpVersion = "^unknown_http_version^"
        internal const val UnknownHost = "^unknown_host^"
        internal const val UnknownPath = "/^unknown_path^"
        internal const val UnknownProtocol = "^unknown_protocol^"
    }

    internal var _method: String? = null

    /**
     * 请求的方法，需要是 HTTP 协议才可以解析
     * */
    val method: String get() = _method!!

    internal var _isHttp: Boolean? = null

    /**
     * true 表示当前请求为 HTTP 请求，false 表示当前请求为 HTTPS 请求，null 表示未知协议
     * */
    val isHttp: Boolean? get() = _isHttp

    internal var _httpVersion: String? = null

    /**
     * 若为 HTTP 请求，则为 HTTP 版本，否则为 null
     * */
    val httpVersion: String get() = _httpVersion ?: UnknownHttpVersion

    internal var _host: String? = null

    /**
     * 请求的域名，需要是 HTTP 协议才可以解析
     * */
    val host: String get() = _host ?: UnknownHost

    internal var _path: String? = null

    /**
     * 请求的路径，需要是 HTTP 协议才可以解析
     * */
    val path: String get() = _path ?: UnknownPath

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
                UnknownProtocol
            }
        }

}

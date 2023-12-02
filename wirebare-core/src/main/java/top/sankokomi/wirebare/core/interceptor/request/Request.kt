@file:Suppress("PropertyName")

package top.sankokomi.wirebare.core.interceptor.request

import top.sankokomi.wirebare.core.interceptor.response.Response
import java.io.Serializable

/**
 * 请求信息
 * */
data class Request internal constructor(
    /**
     * 请求的方法，需要是 HTTP 协议才可以解析
     * */
    var method: String? = null,

    /**
     * true 表示当前请求为 HTTP 请求
     * */
    var isHttp: Boolean = false,

    /**
     * 若为 HTTP 请求，则为 HTTP 版本，否则为 null
     * */
    var httpVersion: String? = null,

    /**
     * 请求的域名，需要是 HTTP 协议才可以解析
     * */
    var host: String? = null,

    /**
     * 请求的路径，需要是 HTTP 协议才可以解析
     * */
    var path: String? = null,

    /**
     * 原始的请求头，包含的是最原始的请求头信息
     * */
    var originHead: String? = null,

    /**
     * 整个请求头，已经以 \r\n 为间隔分隔好
     * */
    var formatHead: List<String>? = null
) : Serializable {

    /**
     * 请求的 URL ，需要是 HTTP 协议才可以解析
     * */
    val url: String?
        get() {
            if (host == null || path == null) {
                return null
            }
            return if (isHttp) "http://${host}${path}" else null
        }

}

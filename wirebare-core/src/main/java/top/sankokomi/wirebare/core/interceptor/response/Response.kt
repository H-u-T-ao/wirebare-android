package top.sankokomi.wirebare.core.interceptor.response

import top.sankokomi.wirebare.core.interceptor.request.Request
import java.io.Serializable

data class Response internal constructor(
    /**
     * 当前响应对应的请求，如果当前响应包
     * */
    var request: Request? = null,
    var isHttp: Boolean = false,
    var httpVersion: String? = null,
    var rspStatus: String? = null,
    var originHead: String? = null,
    var formatHead: List<String>? = null
) : Serializable
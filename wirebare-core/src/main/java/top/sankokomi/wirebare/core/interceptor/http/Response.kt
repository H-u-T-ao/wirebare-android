package top.sankokomi.wirebare.core.interceptor.http

import java.io.Serializable

data class Response internal constructor(
    var destinationAddress: String? = null,
    var url: String? = null,
    var isHttp: Boolean = false,
    var httpVersion: String? = null,
    var rspStatus: String? = null,
    var originHead: String? = null,
    var formatHead: List<String>? = null
) : Serializable
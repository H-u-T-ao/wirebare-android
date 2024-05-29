package top.sankokomi.wirebare.core.interceptor.http

import java.io.Serializable

class HttpResponse internal constructor() : Serializable {

    var requestTime: Long? = null
        internal set

    /**
     * 来源端口号
     * */
    var sourcePort: Short? = null
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
    var url: String? = null
        internal set
    var isHttps: Boolean? = null
        internal set
    var httpVersion: String? = null
        internal set
    var rspStatus: String? = null
        internal set
    var originHead: String? = null
        internal set
    var formatHead: List<String>? = null
        internal set
    internal var hostInternal: String? = null
    internal var isPlaintext: Boolean? = null
    var host: String? = null
        internal set
    var contentType: String? = null
        internal set
    var contentEncoding: String? = null
        internal set
}
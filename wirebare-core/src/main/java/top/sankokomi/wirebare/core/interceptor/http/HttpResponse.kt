package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpResponse
import java.io.Serializable

class HttpResponse internal constructor() : TcpResponse(), Serializable {
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
    var host: String? = null
        internal set
}
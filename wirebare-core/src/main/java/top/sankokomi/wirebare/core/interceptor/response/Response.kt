package top.sankokomi.wirebare.core.interceptor.response

import top.sankokomi.wirebare.core.interceptor.request.Request
import java.io.Serializable

class Response internal constructor() : Serializable {

    var request: Request? = null

    var isHttp: Boolean? = null

    var httpVersion: String? = null

    var rspStatus: String? = null

    var originHead: String? = null

    var formatHead: List<String>? = null

    var rspString: String? = null

}
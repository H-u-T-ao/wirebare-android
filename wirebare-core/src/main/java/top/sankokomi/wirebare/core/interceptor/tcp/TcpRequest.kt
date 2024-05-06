package top.sankokomi.wirebare.core.interceptor.tcp

import java.io.Serializable

open class TcpRequest internal constructor() : Serializable {
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
}
package top.sankokomi.wirebare.core.interceptor.tcp

open class TcpRequest internal constructor() {
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
}
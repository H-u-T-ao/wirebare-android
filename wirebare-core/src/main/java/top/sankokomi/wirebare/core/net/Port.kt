package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.convertPortToString

/**
 * 端口号
 * */
data class Port(
    internal val port: Short
) {

    override fun toString(): String {
        return port.convertPortToString
    }

}
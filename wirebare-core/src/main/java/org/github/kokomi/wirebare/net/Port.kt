package org.github.kokomi.wirebare.net

import org.github.kokomi.wirebare.util.convertPortToString

/**
 * 端口号
 * */
internal data class Port(
    internal val port: Short
) {

    override fun toString(): String {
        return port.convertPortToString
    }

}
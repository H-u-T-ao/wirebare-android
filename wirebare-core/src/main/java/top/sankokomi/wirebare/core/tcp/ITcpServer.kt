package top.sankokomi.wirebare.core.tcp

import top.sankokomi.wirebare.core.net.Port

interface ITcpServer {
    val port: Port
    fun start()
}
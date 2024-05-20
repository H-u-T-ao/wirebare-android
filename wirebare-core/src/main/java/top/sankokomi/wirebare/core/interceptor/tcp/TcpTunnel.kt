package top.sankokomi.wirebare.core.interceptor.tcp

import java.nio.ByteBuffer

interface TcpTunnel {
    fun writeToRemoteServer(buffer: ByteBuffer)
    fun writeToLocalClient(buffer: ByteBuffer)
}
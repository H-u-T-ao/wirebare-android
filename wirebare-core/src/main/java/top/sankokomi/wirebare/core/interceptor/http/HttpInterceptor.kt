package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpInterceptor
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

interface HttpInterceptor : TcpInterceptor<HttpInterceptChain>
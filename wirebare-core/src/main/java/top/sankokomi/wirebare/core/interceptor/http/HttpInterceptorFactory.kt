package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.InterceptorFactory
import top.sankokomi.wirebare.core.net.TcpSession

interface HttpInterceptorFactory : InterceptorFactory<HttpInterceptChain, HttpInterceptor, TcpSession>
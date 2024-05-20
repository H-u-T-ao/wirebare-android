package top.sankokomi.wirebare.core.interceptor.http

interface HttpInterceptorFactory {
    fun create(): HttpInterceptor
}
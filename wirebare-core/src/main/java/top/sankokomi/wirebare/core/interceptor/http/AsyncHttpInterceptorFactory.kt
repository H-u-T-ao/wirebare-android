package top.sankokomi.wirebare.core.interceptor.http

interface AsyncHttpInterceptorFactory {
    fun create(): AsyncHttpInterceptor
}
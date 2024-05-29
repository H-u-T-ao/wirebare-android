package top.sankokomi.wirebare.core.interceptor.http.async

interface AsyncHttpInterceptorFactory {
    fun create(): AsyncHttpInterceptor
}
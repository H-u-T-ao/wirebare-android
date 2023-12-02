package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.InterceptorFactory

interface HttpInterceptorFactory : InterceptorFactory<HttpInterceptChain, HttpInterceptor>
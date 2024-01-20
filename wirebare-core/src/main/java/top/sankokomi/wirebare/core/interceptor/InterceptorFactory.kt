package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session

/**
 * 拦截器创建工厂
 *
 * @param CHAIN 此拦截器所对应拦截器责任链类型
 * @param INTERCEPTOR 此拦截器对应的拦截器类型
 * */
interface InterceptorFactory<CHAIN : InterceptorChain<*>, INTERCEPTOR : Interceptor<CHAIN, SESSION>, SESSION : Session<*>> {

    /**
     * 创建拦截器
     *
     * @return 返回拦截器
     * */
    fun create(): INTERCEPTOR

}
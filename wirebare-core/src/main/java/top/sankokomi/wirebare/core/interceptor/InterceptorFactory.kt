package top.sankokomi.wirebare.core.interceptor

/**
 * 拦截器创建工厂
 *
 * @param C 此拦截器所对应拦截器责任链类型
 * @param I 此拦截器对应的拦截器类型
 * */
interface InterceptorFactory<C : InterceptorChain, I : Interceptor<C>> {

    /**
     * 创建拦截器
     *
     * @return 返回拦截器
     * */
    fun create(): I

}
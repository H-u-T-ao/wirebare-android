package top.sankokomi.wirebare.core.common

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptorFactory
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor

class WireBareConfiguration internal constructor() {

    /**
     * 代理服务的传输单元大小，默认 4096 字节，建议不要设置得太小
     * */
    var mtu: Int = 4096

    /**
     * TCP 代理服务器的数量，默认 1 个，多个代理服务器会构建多个拦截器
     * */
    var tcpProxyServerCount: Int = 1

    /**
     * TUN 网卡 ip 地址
     * */
    internal var address: String = "10.1.10.1"

    /**
     * TUN 网卡 ip 地址前缀长度
     * */
    internal var prefixLength: Int = 32

    /**
     * TUN 网卡 ip 地址以及地址的前缀长度
     *
     * 建议设置几乎不会冲突的 ip 地址，例如 A 类地址
     *
     * 默认 10.1.10.1/32
     * */
    var proxyAddress: Pair<String, Int>
        get() = address to prefixLength
        set(proxyAddress) {
            address = proxyAddress.first
            prefixLength - proxyAddress.second
        }

    /**
     * 增加路由地址，增加路由地址可以对 ip 包进行过滤，只允许指定路由地址列表中的流量通过代理
     *
     * 若希望代理全部流量，设置为 0.0.0.0/0 即可
     * */
    fun addRoutes(vararg route: Pair<String, Int>) {
        routes.addAll(route)
    }

    /**
     * 增加 DNS 服务器
     * */
    fun addDnsServers(vararg dnsServer: String) {
        dnsServers.addAll(dnsServer)
    }

    /**
     * 增加代理的应用，此函数与 [addDisallowedApplications] 只能二选一执行
     *
     * 注意：母应用必须被代理，且无需您手动添加到代理应用列表
     *
     * @see [addDisallowedApplications]
     * */
    fun addAllowedApplications(vararg packageName: String) {
        allowedApplications.addAll(packageName)
    }

    /**
     * 增加不允许代理的应用，此函数与 [addAllowedApplications] 只能二选一执行
     *
     * 注意：母应用必须被代理，请不要在此函数中添加母应用的包名
     *
     * @see [addAllowedApplications]
     * */
    fun addDisallowedApplications(vararg packageName: String) {
        disallowedApplications.addAll(packageName)
    }

    fun setHttpInterceptorFactories(factories: List<HttpInterceptorFactory>) {
        httpInterceptorFactories.clear()
        httpInterceptorFactories.addAll(factories)
    }

    fun setHttpInterceptorFactories(vararg factories: HttpInterceptorFactory) {
        httpInterceptorFactories.clear()
        httpInterceptorFactories.addAll(factories)
    }

    fun setHttpInterceptorFactory(factory: HttpInterceptorFactory) {
        httpInterceptorFactories.clear()
        httpInterceptorFactories.add(factory)
    }

    fun setHttpInterceptorFactory(factory: () -> HttpInterceptor) {
        httpInterceptorFactories.clear()
        httpInterceptorFactories.add(
            object : HttpInterceptorFactory {
                override fun create(): HttpInterceptor {
                    return factory()
                }
            }
        )
    }

    internal val routes: MutableSet<Pair<String, Int>> = hashSetOf()

    internal val dnsServers: MutableSet<String> = hashSetOf()

    internal val allowedApplications: MutableSet<String> = hashSetOf()

    internal val disallowedApplications: MutableSet<String> = hashSetOf()

    internal val httpInterceptorFactories: MutableList<HttpInterceptorFactory> = mutableListOf()

    internal fun copy(): WireBareConfiguration {
        return WireBareConfiguration().also {
            it.mtu = mtu
            it.address = address
            it.prefixLength = prefixLength
            it.proxyAddress = proxyAddress
            it.routes.addAll(routes)
            it.dnsServers.addAll(dnsServers)
            it.allowedApplications.addAll(allowedApplications)
            it.disallowedApplications.addAll(disallowedApplications)
            it.httpInterceptorFactories.addAll(httpInterceptorFactories)
        }
    }

}
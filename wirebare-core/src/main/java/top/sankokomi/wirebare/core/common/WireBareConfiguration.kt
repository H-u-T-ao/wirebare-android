package top.sankokomi.wirebare.core.common

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptorFactory
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.ssl.JKS

class WireBareConfiguration internal constructor() {

    var jks: JKS? = null

    /**
     * 代理服务的传输单元大小，默认 4096 字节，建议不要设置得太小
     * */
    var mtu: Int = 4096

    /**
     * TCP 代理服务器的数量，默认 1 个，多个代理服务器会构建多个拦截器
     * */
    var tcpProxyServerCount: Int = 1

    /**
     * TUN 网卡 ipv4 地址
     * */
    internal var ipv4Address: String = "10.1.10.1"

    /**
     * TUN 网卡 ipv4 地址前缀长度
     * */
    internal var ipv4PrefixLength: Int = 32

    /**
     * TUN 网卡 ipv4 地址以及地址的前缀长度
     *
     * 建议设置几乎不会冲突的 ipv4 地址，例如 A 类地址
     *
     * 默认 10.1.10.1/32
     * */
    var ipv4ProxyAddress: Pair<String, Int>
        get() = ipv4Address to ipv4PrefixLength
        set(proxyAddress) {
            ipv4Address = proxyAddress.first
            ipv4PrefixLength - proxyAddress.second
        }

    /**
     * 是否启用 ipv6
     *
     * 当然启动的前提是设备和网络都必须支持 ipv6
     *
     * 如果不支持 ipv6 但启用了，那么还是会代理 ipv6 流量，但不会有任何响应
     *
     * 一般来说，ipv6 失败以后会退化成为 ipv4
     * */
    var enableIpv6: Boolean = false

    /**
     * TUN 网卡 ipv6 地址
     *
     * 请使用全称，不要用 :: 省略
     * */
    internal var ipv6Address: String = "a:0:0:1:a:0:0:1"

    /**
     * TUN 网卡 ipv6 地址前缀长度
     * */
    internal var ipv6PrefixLength: Int = 128

    /**
     * TUN 网卡 ipv6 地址以及地址的前缀长度
     * */
    var ipv6ProxyAddress: Pair<String, Int>
        get() = ipv6Address to ipv6PrefixLength
        set(proxyAddress) {
            ipv6Address = proxyAddress.first
            ipv6PrefixLength - proxyAddress.second
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
            it.jks = jks
            it.mtu = mtu
            it.ipv4Address = ipv4Address
            it.ipv4PrefixLength = ipv4PrefixLength
            it.ipv4ProxyAddress = ipv4ProxyAddress.copy()
            it.ipv6Address = ipv6Address
            it.ipv6PrefixLength = ipv6PrefixLength
            it.enableIpv6 = enableIpv6
            it.ipv6ProxyAddress = ipv6ProxyAddress.copy()
            it.routes.addAll(routes)
            it.dnsServers.addAll(dnsServers)
            it.allowedApplications.addAll(allowedApplications)
            it.disallowedApplications.addAll(disallowedApplications)
            it.httpInterceptorFactories.addAll(httpInterceptorFactories)
        }
    }

}
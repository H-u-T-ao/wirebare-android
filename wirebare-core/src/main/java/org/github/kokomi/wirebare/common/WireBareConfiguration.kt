package org.github.kokomi.wirebare.common

import android.app.Notification
import android.net.VpnService
import org.github.kokomi.wirebare.interceptor.InterceptorFactory
import org.github.kokomi.wirebare.interceptor.RequestChain
import org.github.kokomi.wirebare.interceptor.RequestInterceptor
import org.github.kokomi.wirebare.service.WireBareProxyService
import org.github.kokomi.wirebare.util.defaultNotification

class WireBareConfiguration internal constructor() {

    /**
     * 代理服务的传输单元大小，默认 4096 字节，建议不要设置得太小
     * */
    var mtu: Int = 4096

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
     * 通知通道 ID ，默认 WireBareProxyService
     * */
    var channelId: String = "WireBareProxyService"

    /**
     * 通知 ID ，默认 222
     * */
    var notificationId: Int = 222

    /**
     * 创建通知，默认 [VpnService.defaultNotification]
     *
     * 代理抓包对于用户来说有危险性，因此前台服务并显示通知用户是必须的
     * */
    var notification: WireBareProxyService.() -> Notification = { defaultNotification(channelId) }

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
     * 增加代理的应用，此函数与 [addDisallowedApplication] 只能二选一执行
     *
     * 注意：母应用必须被代理，且无需您手动添加到代理应用列表
     *
     * @see [addDisallowedApplication]
     * */
    fun addAllowedApplications(vararg packageName: String) {
        allowedApplications.addAll(packageName)
    }

    /**
     * 增加不允许代理的应用，此函数与 [addAllowedApplication] 只能二选一执行
     *
     * 注意：母应用必须被代理，请不要在此函数中添加母应用的包名
     *
     * @see [addAllowedApplication]
     * */
    fun addDisallowedApplications(vararg packageName: String) {
        disallowedApplications.addAll(packageName)
    }

    /**
     * 增加请求拦截器
     * */
    fun addRequestInterceptors(vararg factories: InterceptorFactory<RequestChain, RequestInterceptor>) {
        requestInterceptorFactories.addAll(factories)
    }

    /**
     * 增加请求拦截器
     * */
    fun addRequestInterceptors(vararg factories: () -> RequestInterceptor) {
        val list = mutableListOf<InterceptorFactory<RequestChain, RequestInterceptor>>()
        for (factory in factories) {
            list.add(object : InterceptorFactory<RequestChain, RequestInterceptor> {
                override fun create(): RequestInterceptor = factory()
            })
        }
        requestInterceptorFactories.addAll(list)
    }

    internal val routes: MutableSet<Pair<String, Int>> = hashSetOf()

    internal val dnsServers: MutableSet<String> = hashSetOf()

    internal val allowedApplications: MutableSet<String> = hashSetOf()

    internal val disallowedApplications: MutableSet<String> = hashSetOf()

    internal val requestInterceptorFactories: MutableList<InterceptorFactory<RequestChain, RequestInterceptor>> =
        mutableListOf()

}
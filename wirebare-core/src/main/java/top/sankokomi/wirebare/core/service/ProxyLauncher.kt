package top.sankokomi.wirebare.core.service

import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.service.PacketDispatcher.Companion.dispatchWith

/**
 * VPN 代理服务的启动器
 * */
internal class ProxyLauncher private constructor(
    internal val configuration: WireBareConfiguration,
    internal val proxyService: WireBareProxyService
) : CoroutineScope by proxyService {

    companion object {
        internal infix fun WireBareProxyService.launchWith(
            configuration: WireBareConfiguration
        ): ParcelFileDescriptor? {
            return ProxyLauncher(configuration, this).launch()
        }
    }

    private fun launch(): ParcelFileDescriptor? {
        if (!isActive) return null
        // 配置 VPN 服务
        val builder = proxyService.Builder().also { builder ->
            with(configuration) {
                builder.setMtu(mtu)
                    .addAddress(ipv4Address, ipv4PrefixLength)
                    .allowFamily(OsConstants.AF_INET)
                    .setBlocking(true)
                if (enableIpv6) {
                    builder.addAddress(ipv6Address, ipv6PrefixLength)
                        .allowFamily(OsConstants.AF_INET6)
                }
                for (route in routes) {
                    builder.addRoute(route.first, route.second)
                }
                for (dns in dnsServers) {
                    builder.addDnsServer(dns)
                }
                // 允许和不允许应用只能配置其中一个
                for (application in allowedApplications) {
                    builder.addAllowedApplication(application)
                }
                if (allowedApplications.isNotEmpty()) {
                    builder.addAllowedApplication(proxyService.packageName)
                }
                if (disallowedApplications.contains(proxyService.packageName)) {
                    throw IllegalArgumentException("母应用必须接入到代理服务中")
                }
                for (application in disallowedApplications) {
                    builder.addDisallowedApplication(application)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    builder.setMetered(false)
                }
            }
        }

        return this dispatchWith builder
    }

}
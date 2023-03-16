package org.github.kokomi.wirebare.service

import android.os.Build
import android.system.OsConstants.AF_INET
import kotlinx.coroutines.*
import org.github.kokomi.wirebare.common.WireBareConfiguration
import org.github.kokomi.wirebare.service.PacketDispatcher.Companion.dispatchWith

internal class ProxyLauncher private constructor(
    internal val configuration: WireBareConfiguration,
    internal val proxyService: WireBareProxyService
) : CoroutineScope by proxyService {

    companion object {
        internal infix fun WireBareProxyService.launchWith(configuration: WireBareConfiguration) {
            ProxyLauncher(configuration, this).launch()
        }
    }

    private fun launch() {
        if (!isActive) return
        // 配置 VPN 服务
        val builder = proxyService.Builder().also { builder ->
            with(configuration) {
                builder.setMtu(mtu)
                    .addAddress(address, prefixLength)
                    .allowFamily(AF_INET)
                    .setBlocking(true)
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

        this dispatchWith builder
    }

}
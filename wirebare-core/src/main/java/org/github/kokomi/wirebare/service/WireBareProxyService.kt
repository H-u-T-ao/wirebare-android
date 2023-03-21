package org.github.kokomi.wirebare.service

import android.app.Notification
import android.content.Intent
import android.net.VpnService
import android.os.Build
import org.github.kokomi.wirebare.common.WireBare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.github.kokomi.wirebare.service.ProxyLauncher.Companion.launchWith
import org.github.kokomi.wirebare.util.defaultNotification

abstract class WireBareProxyService : VpnService(),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    companion object {
        internal const val WIREBARE_ACTION_PROXY_VPN_START =
            "org.github.kokomi.wirebare.action.Start"

        internal const val WIREBARE_ACTION_PROXY_VPN_STOP =
            "org.github.kokomi.wirebare.action.Stop"
    }

    /**
     * 通知通道 ID ，默认 WireBareProxyService
     * */
    protected open var channelId: String = "WireBareProxyService"

    /**
     * 通知 ID ，默认 222
     * */
    protected open var notificationId: Int = 222

    /**
     * 创建通知，默认 [VpnService.defaultNotification]
     *
     * 代理抓包对于用户来说有危险性，因此前台服务并显示通知用户是必须的
     * */
    protected open var notification: WireBareProxyService.() -> Notification =
        { defaultNotification(channelId) }


    override fun onCreate() {
        super.onCreate()
        WireBare notifyVpnStatusChanged WireBare.WIREBARE_STATUS_SERVICE_CREATE
    }

    final override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return START_NOT_STICKY
        when (intent.action) {
            WIREBARE_ACTION_PROXY_VPN_START -> startWireBare()
            WIREBARE_ACTION_PROXY_VPN_STOP -> stopWireBare()
            else -> throw IllegalArgumentException("意料之外的 Action")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startWireBare() {
        val configuration = WireBare.configuration.apply {
            startForeground(notificationId, notification())
        }
        this launchWith configuration
    }

    private fun stopWireBare() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        WireBare notifyVpnStatusChanged WireBare.WIREBARE_STATUS_SERVICE_DESTROY
        cancel()
    }

}
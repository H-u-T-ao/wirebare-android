package top.sankokomi.wirebare.core.common

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.util.LogLevel
import top.sankokomi.wirebare.core.util.WireBareLogger

object WireBare {

    private lateinit var appContext: Context

    private var _configuration: WireBareConfiguration? = null

    private val listeners: MutableSet<IProxyStatusListener> = hashSetOf()

    private val eventListeners: MutableSet<IImportantEventListener> = hashSetOf()

    /**
     * [WireBareProxyService] 的实时状态
     * */
    var proxyStatus: ProxyStatus = ProxyStatus.DEAD
        private set

    /**
     * 启动代理服务
     *
     * @param configuration WireBare 的配置
     *
     * @see [WireBareConfiguration]
     * @see [stopProxy]
     * */
    @MainThread
    fun startProxy(configuration: WireBareConfiguration.() -> Unit) {
        if (proxyStatus == ProxyStatus.ACTIVE) return
        notifyVpnStatusChanged(ProxyStatus.STARTING)
        _configuration = WireBareConfiguration()
            .apply(configuration)
        val intent = Intent(WireBareProxyService.WIREBARE_ACTION_PROXY_VPN_START).apply {
            `package` = appContext.packageName
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    /**
     * 结束代理服务
     *
     * @see [startProxy]
     * */
    @MainThread
    fun stopProxy() {
        if (proxyStatus == ProxyStatus.DEAD) return
        notifyVpnStatusChanged(ProxyStatus.DYING)
        val intent = Intent(WireBareProxyService.WIREBARE_ACTION_PROXY_VPN_STOP).apply {
            `package` = appContext.packageName
        }
        appContext.startService(intent)
    }

    /**
     * 注册代理服务状态监听器，需要进行注销
     *
     * @see [IProxyStatusListener]
     * @see [removeVpnProxyStatusListener]
     * */
    @MainThread
    fun addVpnProxyStatusListener(listener: IProxyStatusListener) {
        listener.onVpnStatusChanged(ProxyStatus.DEAD, proxyStatus)
        listeners.add(listener)
    }

    /**
     * 注销代理服务状态监听器
     *
     * @see [IProxyStatusListener]
     * @see [addVpnProxyStatusListener]
     * */
    @MainThread
    fun removeVpnProxyStatusListener(listener: IProxyStatusListener): Boolean {
        return listeners.remove(listener)
    }

    /**
     * 注册重要事件监听器
     *
     * @see [IImportantEventListener]
     * @see [EventSynopsis]
     * @see [removeImportantEventListener]
     * */
    @MainThread
    fun addImportantEventListener(listener: IImportantEventListener) {
        eventListeners.add(listener)
    }

    /**
     * 注销代理服务状态监听器
     *
     * @see [IImportantEventListener]
     * @see [EventSynopsis]
     * @see [addImportantEventListener]
     * */
    @MainThread
    fun removeImportantEventListener(listener: IImportantEventListener): Boolean {
        return eventListeners.remove(listener)
    }

    /**
     * 配置日志等级
     *
     * @see [LogLevel]
     * */
    @LogLevel
    var logLevel: Int
        get() = WireBareLogger.LOG_LEVEL
        set(level) {
            WireBareLogger.LOG_LEVEL = level
        }

    internal infix fun attach(context: Context) {
        appContext = context
    }

    internal fun notifyVpnStatusChanged(newStatus: ProxyStatus) {
        Handler(Looper.getMainLooper()).post {
            WireBareLogger.info("statusChange: old = $proxyStatus, new = $newStatus")
            if (newStatus == proxyStatus) return@post
            val oldStatus = proxyStatus
            proxyStatus = newStatus
            listeners.removeAll { listener ->
                listener.onVpnStatusChanged(oldStatus, newStatus)
            }
        }
    }

    internal fun postImportantEvent(event: ImportantEvent) {
        eventListeners.forEach { listener ->
            listener.onPost(event)
        }
    }

    internal val configuration: WireBareConfiguration
        get() {
            val config = _configuration
            if (config != null) return config
            throw NullPointerException("WireBare 配置为空")
        }

}
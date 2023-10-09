package top.sankokomi.wirebare.core.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.util.LogLevel
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.lang.ref.WeakReference

object WireBare {

    /**
     * [WireBareProxyService] 的实时状态
     * */
    var vpnProxyServiceStatus: VpnProxyServiceStatus = VpnProxyServiceStatus.DEAD
        private set

    private lateinit var appContext: Context

    private var _configuration: WireBareConfiguration? = null

    private val listeners: MutableSet<WeakReference<IProxyStatusListener>> = hashSetOf()

    /**
     * 准备代理服务
     *
     * @param onResult true 表示用户授权，准备成功，否则表示用户拒绝
     * */
    fun prepareVpnProxyService(
        activity: ComponentActivity,
        onResult: (Boolean) -> Unit
    ) {
        val intent = VpnService.prepare(appContext) ?: return onResult(true)
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
             onResult(it.resultCode == Activity.RESULT_OK)
        }.launch(intent)
    }

    /**
     * 启动代理服务
     *
     * @param configuration WireBare 的配置
     *
     * @see [WireBareConfiguration]
     * @see [stopProxy]
     * */
    fun startProxy(configuration: WireBareConfiguration.() -> Unit) {
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
    fun stopProxy() {
        val intent = Intent(WireBareProxyService.WIREBARE_ACTION_PROXY_VPN_STOP).apply {
            `package` = appContext.packageName
        }
        appContext.startService(intent)
    }

    /**
     * 注册代理服务状态监听器，内部会转换为弱引用，因此可以不注销，不会发生内存泄露
     *
     * @see [IProxyStatusListener]
     * @see [SimpleProxyStatusListener]
     * */
    fun addVpnProxyStatusListener(listener: IProxyStatusListener) {
        listeners.add(WeakReference(listener))
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

    internal fun notifyVpnStatusChanged(newStatus: VpnProxyServiceStatus) {
        Handler(Looper.getMainLooper()).post {
            if (newStatus == vpnProxyServiceStatus) return@post
            val oldStatus = vpnProxyServiceStatus
            vpnProxyServiceStatus = newStatus
            listeners.forEach {
                it.get()?.onVpnStatusChanged(oldStatus, newStatus)
            }
        }
    }

    internal val configuration: WireBareConfiguration
        get() {
            val config = _configuration
            if (config != null) return config
            throw NullPointerException("WireBare 配置为空")
        }

}
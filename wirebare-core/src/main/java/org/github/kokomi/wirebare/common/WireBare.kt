package org.github.kokomi.wirebare.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import org.github.kokomi.wirebare.service.*
import org.github.kokomi.wirebare.util.LogLevel
import org.github.kokomi.wirebare.util.WireBareLogger

object WireBare {

    /**
     * 表示代理服务正在启动
     * */
    const val WIREBARE_STATUS_SERVICE_CREATE = 0

    /**
     * 表示代理服务正在销毁
     * */
    const val WIREBARE_STATUS_SERVICE_DESTROY = 1

    /**
     * 表示代理服务器已经准备完毕
     * */
    const val WIREBARE_STATUS_PROXY_SERVER_PREPARED = 2

    private lateinit var appContext: Context

    private var _configuration: WireBareConfiguration? = null

    /**
     * true 表示代理服务正在运行，false 代表代理服务未在运行
     * */
    var alive: Boolean = false
        private set

    private var status: Int = WIREBARE_STATUS_SERVICE_DESTROY

    private val listeners: MutableSet<IProxyStatusListener> by lazy { hashSetOf() }

    /**
     * 在 [Activity] 中准备代理服务，若已经授权代理服务，则返回 true
     *
     * 若返回 false ，请在 [Activity.onActivityResult] 中对请求结果进行处理
     *
     * @see [handlePrepareResult]
     * */
    fun prepareProxy(
        activity: Activity,
        requestCode: Int,
        bundle: Bundle? = null
    ): Boolean {
        val intent = VpnService.prepare(appContext) ?: return true
        activity.startActivityForResult(intent, requestCode, bundle)
        return false
    }

    /**
     * 在 [Activity.onActivityResult] 中添加此函数，即可对代理服务授权结果进行处理
     *
     * @param requestCode [Activity.onActivityResult] 中传入的 requestCode
     * @param resultCode [Activity.onActivityResult] 中传入的 resultCode
     * @param targetCode 在 [prepareProxy] 中传入的 requestCode
     * @param result 请求结果回调
     * */
    fun handlePrepareResult(
        requestCode: Int,
        resultCode: Int,
        targetCode: Int,
        result: (Boolean) -> Unit
    ) {
        if (requestCode == targetCode) {
            result(resultCode == Activity.RESULT_OK)
        }
    }

    /**
     * 启动代理服务
     *
     * @param configuration WireBare 的配置
     *
     * @see [WireBareConfiguration]
     * @see [stopProxy]
     * */
    infix fun startProxy(configuration: WireBareConfiguration.() -> Unit) {
        _configuration = WireBareConfiguration().apply(configuration)
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
     * 注册代理服务状态监听器
     *
     * @return 若此 [listener] 原本不在监听者集合中，则添加并返回 true ，否则返回 false
     *
     * @see [removeProxyStatusListener]
     * @see [IProxyStatusListener]
     * @see [SimpleProxyStatusListener]
     * */
    infix fun addProxyStatusListener(listener: IProxyStatusListener): Boolean {
        return listeners.add(listener)
    }

    /**
     * 移除代理服务状态监听器
     *
     * @return 若此 [listener] 原本在监听者集合中，则移除并返回 true ，否则返回 false
     *
     * @see [addProxyStatusListener]
     * @see [IProxyStatusListener]
     * @see [SimpleProxyStatusListener]
     * */
    infix fun removeProxyStatusListener(listener: IProxyStatusListener): Boolean {
        return listeners.remove(listener)
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

    internal infix fun notifyVpnStatusChanged(status: Int) {
        Handler(Looper.getMainLooper()).post {
            if (status == WireBare.status) return@post
            WireBare.status = status
            if (status == WIREBARE_STATUS_SERVICE_CREATE) alive = true
            else if (status == WIREBARE_STATUS_SERVICE_DESTROY) alive = false
            listeners.forEach {
                when (status) {
                    WIREBARE_STATUS_SERVICE_CREATE -> it.onProxyServiceCreate()
                    WIREBARE_STATUS_SERVICE_DESTROY -> it.onProxyServerDestroy()
                    WIREBARE_STATUS_PROXY_SERVER_PREPARED -> it.onProxyServerPrepared()
                }
                it.onStatusChanged(status)
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
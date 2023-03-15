package org.github.kokomi.wirebare.common

import android.net.VpnService
import androidx.annotation.MainThread

interface IProxyStatusListener {

    /**
     * 当服务启动时回调，保证在主线程上回调
     *
     * [VpnService.onCreate] 执行完毕后立即回调此函数
     *
     * 会先回调 [onProxyServiceCreate] 再回调 [onStatusChanged]
     * */
    @MainThread
    fun onProxyServiceCreate()

    /**
     * 当服务关闭时回调，保证在主线程上回调
     *
     * [VpnService.onDestroy] 执行完毕后立即回调此函数
     *
     * 会先回调 [onProxyServerDestroy] 再回调 [onStatusChanged]
     * */
    @MainThread
    fun onProxyServerDestroy()

    /**
     * 当代理服务器准备完毕时回调，保证在主线程上回调
     *
     * 会先回调 [onProxyServerPrepared] 再回调 [onStatusChanged]
     * */
    @MainThread
    fun onProxyServerPrepared()

    /**
     * 当代理服务状态改变时回调，保证在主线程上回调
     *
     * @see [WireBare.WIREBARE_STATUS_SERVICE_CREATE]
     * @see [WireBare.WIREBARE_STATUS_SERVICE_DESTROY]
     * @see [WireBare.WIREBARE_STATUS_PROXY_SERVER_PREPARED]
     * */
    @MainThread
    fun onStatusChanged(status: Int)

}
package top.sankokomi.wirebare.core.common

import androidx.annotation.MainThread

interface IProxyStatusListener {

    /**
     * @return 返回 true 表示立即移除此监听器
     * */
    @MainThread
    fun onVpnStatusChanged(oldStatus: ProxyStatus, newStatus: ProxyStatus): Boolean

}
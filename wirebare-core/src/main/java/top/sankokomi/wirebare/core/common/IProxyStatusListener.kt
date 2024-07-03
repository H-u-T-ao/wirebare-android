package top.sankokomi.wirebare.core.common

import androidx.annotation.MainThread

interface IProxyStatusListener {

    @MainThread
    fun onVpnStatusChanged(oldStatus: ProxyStatus, newStatus: ProxyStatus): Boolean

}
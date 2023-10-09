package top.sankokomi.wirebare.core.common

import androidx.annotation.MainThread

interface IProxyStatusListener {

    @MainThread
    fun onVpnStatusChanged(oldStatus: VpnProxyServiceStatus, newStatus: VpnProxyServiceStatus)

}
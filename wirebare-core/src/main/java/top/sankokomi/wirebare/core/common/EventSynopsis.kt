package top.sankokomi.wirebare.core.common

enum class EventSynopsis {
    /**
     * 某个 ipv6 地址不可达哦
     *
     * 更进一步：当前设备/网络疑似不支持 IPv6
     *
     * 当然也可能是网络错误导致的
     * */
    IPV6_UNREACHABLE,

    /**
     * 这一般是网络错误或者 ipv4 地址本来就是错的
     * */
    IPV4_UNREACHABLE
}
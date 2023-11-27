package top.sankokomi.wirebare.core.common

enum class ProxyStatus {

    /**
     * 表示当前代理服务正在运行
     * */
    ACTIVE,

    /**
     * 表示当前代理服务启动中
     * */
    STARTING,

    /**
     * 表示当前代理服务正在结束
     * */
    DYING,

    /**
     * 表示当前代理服务已停止，初始态
     * */
    DEAD

}
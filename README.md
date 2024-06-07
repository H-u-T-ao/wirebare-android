# WireBare

WireBare 是一个基于 Andoird VPN Service 开发的 Android 抓包框架

整个项目是一个完整的 Android 应用程序，其中的 wirebare-core 模块为核心的抓包模块，app 模块则提供了一些拓展功能和简单的用户界面

在高版本的 Android 系统中的 HTTPS 的拦截抓包功能需要先安装代理服务器根证书到 Android 系统的根证书目录下



### 功能概览

#### 网际层

- 支持 IPv4 和 IPv6 的代理抓包
- 支持 IP 协议解析

#### 传输层

- 支持 TCP 透明代理、拦截抓包
- 支持 UDP 透明代理

#### 应用层

- 支持 HTTP 协议解析
- 支持 HTTPS 加解密（基于 TLSv1.2，需要先为 Android 安装代理服务器根证书）



### 注册代理服务

WireBare 代理服务是一个抽象类，你可以继承它然后进行自定义，SimpleWireBareProxyService 是它最简单的实现子类

```kotlin
class SimpleWireBareProxyService : WireBareProxyService()
```



在 AndroidManifest.xml 文件的 application 标签中添加如下代码来注册 WireBare 代理服务（以 SimpleWireBareProxyService 为例）

```xml
<application>
    <service
        android:name="top.sankokomi.wirebare.core.service.SimpleWireBareProxyService"
        android:exported="false"
        android:permission="android.permission.BIND_VPN_SERVICE">
        <intent-filter>
            <action android:name="android.net.VpnService" />
            <action android:name="top.sankokomi.wirebare.core.action.Start" />
            <action android:name="top.sankokomi.wirebare.core.action.Stop" />
        </intent-filter>
    </service>
</application>
```



### 准备代理服务

在启动 WireBare 代理服务前需要先进行准备，第一次准备时将会弹出一个用户授权对话框，用户授权后即可启动代理服务

```kotlin
class SimpleActivity : VpnPrepareActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 若授权成功，则会回调 onPrepareSuccess()
        prepareProxy()
    }
    
    override fun onPrepareSuccess() {
        // 可以在这里调整日志等级
        WireBare.logLevel = Level.SILENT
        // 如果需要支持 HTTPS 抓包，则需要配置密钥信息
        WireBare.jks = JKS(...)
        WireBare.startProxy {
            // 传输单元大小
            mtu = 10 * 1024
            // TCP 代理服务器数量
            tcpProxyServerCount = 1
            // VpnService 的 IPv4 地址
            ipv4ProxyAddress = "10.1.10.1" to 32
            // 启用 IPv6 数据包代理
            enableIpv6 = true
            // VpnService 的 IPv6 地址
            ipv6ProxyAddress = "a:a:1:1:a:a:1:1" to 128
            // 路由，如果启用了 IPv6 数据包代理，则需要同时设置 IPv6 数据包的路由
            addRoutes("0.0.0.0" to 0, "::" to 0)
            // 增加要被抓包的应用的包名
            // addAllowedApplications(...)
            // 增加异步 HTTP 拦截器
            // addAsyncHttpInterceptor(...)
            // 增加阻塞 HTTP 拦截器
            // addHttpInterceptor(...)
        }
    }

}
```



### 配置和启动代理服务

准备过后即可随时启动 WireBare 代理服务，上面已经有启动 WireBare 代理服务的简单例子，下面是更加详细的说明

```kotlin
fun start() {
    // 注册代理服务状态监听器，可以监听代理服务的启动和销毁以及代理服务器的启动
    // 需要注销，调用 WireBare.removeVpnProxyStatusListener(...)
    WireBare.addVpnProxyStatusListener(...)
    // 直接访问以下变量也可以随时获取代理服务的运行状态
    val vpnProxyServiceStatus = WireBare.proxyStatus
    
    // 配置 WireBare 日志等级
    WireBare.logLevel = Level.SILENT
    // 配置并启动代理服务
    WireBare.startProxy {
        // 代理服务传输单元大小，单位：字节（默认 4096）
        mtu = 10 * 1024
        
        // TCP 代理服务器数量
        tcpProxyServerCount = 1
        
        // VpnService 的 IPv4 地址
        ipv4ProxyAddress = "10.1.10.1" to 32
        
        // 启用 IPv6 数据包代理
        enableIpv6 = true
        
        // VpnService 的 IPv6 地址
        ipv6ProxyAddress = "a:a:1:1:a:a:1:1" to 128
        
        // 增加代理服务的路由
        // 如果启用了 IPv6 数据包代理，则需要同时设置 IPv6 数据包的路由
        addRoutes("0.0.0.0" to 0, "::" to 0)
        
        // 增加 DNS 服务器
        addDnsServers(...)
        
        // 以下两种设置只能配置其中一种，不能同时配置
        // 母应用默认被代理，无需手动配置
        // 增加被代理的应用
        addAllowedApplications(...)
        // 增加不允许代理的应用
        addDisallowedApplications(...)
        
        // 增加异步 HTTP 拦截器
        addAsyncHttpInterceptor(...)
        // 增加阻塞 HTTP 拦截器
        addHttpInterceptor(...)
    }
}
```



### 停止代理服务

抓包完毕后，执行以下函数来停止代理服务

```kotlin
fun stop() {
    WireBare.stopProxy()
}
```


# WireBare

​	WireBare 是一个基于 Andoird VPN Service 开发的 Android 抓包框架



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
class SimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        WireBare.prepareVpnProxyService(this) {
            if (it) {
                startProxy()
            } else {
                Toast.makeText(this, "未授权 VPN 服务", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startProxy() {
        // 可以在这里调整日志等级
        WireBare.logLevel = Level.SILENT
        WireBare.startProxy {
            mtu = 7000
            proxyAddress = "10.1.10.1" to 32
            addRoutes("0.0.0.0" to 0)
            // 增加要被抓包的应用的包名
            addAllowedApplications("com.tencent.mm")
            // 在这里加入拦截器，即可进行抓包
            // addRequestInterceptors(...)
        }
    }

}
```



### 配置和启动代理服务

准备过后即可随时启动 WireBare 代理服务，上面已经有启动 WireBare 代理服务的简单例子，下面是更加详细的说明

```kotlin
fun start() {
    // 注册代理服务状态监听器，可以监听代理服务的启动和销毁以及代理服务器的启动
    // 无需注销，内部使用弱引用
    WireBare.addVpnProxyStatusListener(...)
    // 直接访问以下变量也可以随时获取代理服务的运行状态
    val vpnProxyServiceStatus = WireBare.vpnProxyServiceStatus
    
    // 配置 WireBare 日志等级
    WireBare.logLevel = Level.SILENT
    // 配置并启动代理服务
    WireBare.startProxy {
        // 代理服务传输单元大小，单位：字节（默认 4096）
        mtu = 7000
        
        // 代理服务 TUN 网卡 ip 地址（默认 10.1.10.1/32）
        proxyAddress = "10.1.10.1" to 32
        
        // 代理服务的前台服务通知通道 ID（默认 WireBareProxyService）
        channelId = "WireBareProxyService"
        // 代理服务的前台服务通知 ID（默认 222）
        notificationId = 222
        // 代理服务的前台服务的通知（默认显示显示最简单的提示前台服务正在运行的通知）
        notification = { defaultNotification(channelId) }
        
        // 增加代理服务的路由（必选项）
        addRoutes("0.0.0.0" to 0)
        
        // 增加 DNS 服务器（可选项）
        addDnsServers(...)
        
        // 以下两种设置只能配置其中一种，不能同时配置
        // 母应用默认被代理，无需手动配置
        // 增加被代理的应用
        addAllowedApplications(...)
        addAllowedApplications(...)
        // 增加不允许代理的应用
        addDisallowedApplications(...)
        addDisallowedApplications(...)
        
        // 设置请求拦截器，拦截请求数据包
        setRequestInterceptors(...)
        addRequestInterceptors(...)
        
        // 设置响应拦截器，拦截响应数据包
        setResponseInterceptors(...)
        addResponseInterceptors(...)
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


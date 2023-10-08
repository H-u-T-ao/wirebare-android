package top.sankokomi.wirebare.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.HttpRequestUrlInterceptor
import top.sankokomi.wirebare.core.interceptor.Request
import top.sankokomi.wirebare.core.util.Level

class SimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        val rv = findViewById<RecyclerView>(R.id.rv_simple_list)

        if (WireBare.prepareProxy(this, 2222)) {
            startProxy()
        }
    }

    @Suppress("Deprecation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        WireBare.handlePrepareResult(requestCode, resultCode, 2222) {
            if (it) startProxy()
        }
    }

    private fun startProxy() {
        // 可以在这里调整日志等级
        WireBare.logLevel = Level.SILENT
        WireBare.startProxy {
            mtu = 7000
            proxyAddress = "10.1.10.1" to 32
            addRoutes("0.0.0.0" to 0)
            addAllowedApplications("com.tencent.mm")
            // 在这里加入拦截器，即可进行抓包
            // addRequestInterceptors(...)
            addRequestInterceptors({
                object: HttpRequestUrlInterceptor() {
                    override fun onRequest(url: String) {
                    }

                    override fun onRequestFinished(request: Request) {
                    }
                }
            })
        }
    }

}
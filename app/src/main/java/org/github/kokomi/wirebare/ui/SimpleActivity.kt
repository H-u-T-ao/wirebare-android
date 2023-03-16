package org.github.kokomi.wirebare.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.github.kokomi.wirebare.common.WireBare
import org.github.kokomi.wirebare.util.Level

class SimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

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
            addAllowedApplications("com.kokomi.request")
            // 在这里加入拦截器，即可进行抓包
            // addRequestInterceptors(...)
        }
    }

}
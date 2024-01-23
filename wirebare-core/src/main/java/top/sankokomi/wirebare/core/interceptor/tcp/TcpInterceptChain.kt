package top.sankokomi.wirebare.core.interceptor.tcp

import androidx.annotation.CallSuper
import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

abstract class TcpInterceptChain<REQ : TcpRequest, RSP : TcpResponse> :
    InterceptorChain<TcpSession> {

    private val curReqRspMap = hashMapOf<TcpSession, Pair<REQ, RSP>>()

    /**
     * 通过 [session] 拿到当前会话所对应的 [HttpRequest] 和 [HttpResponse]
     * */
    fun curReqRsp(session: TcpSession): Pair<REQ, RSP>? {
        return curReqRspMap[session]
    }

    protected abstract fun newInstanceReqRsp(): Pair<REQ, RSP>

    private fun checkCurReqRsp(session: TcpSession) {
        if (!curReqRspMap.containsKey(session)) {
            curReqRspMap[session] = newInstanceReqRsp().also {
                it.first.run {
                    sourcePort = session.sourcePort.port
                    destinationAddress = session.destinationAddress.string
                    destinationPort = session.destinationPort.port
                }
                it.second.run {
                    sourcePort = session.sourcePort.port
                    destinationAddress = session.destinationAddress.string
                    destinationPort = session.destinationPort.port
                }
            }
        }
    }

    @CallSuper
    override fun processRequestFirst(buffer: ByteBuffer, session: TcpSession) {
        requestReflux = buffer to BufferDirection.RemoteServer
        checkCurReqRsp(session)
        processRequestNext(buffer, session)
    }

    @CallSuper
    override fun processRequestFinishedFirst(session: TcpSession) {
        processRequestFinishedNext(session)
        curReqRspMap.remove(session)
    }

    @CallSuper
    override fun processResponseFirst(buffer: ByteBuffer, session: TcpSession) {
        responseReflux = buffer to BufferDirection.ProxyClient
        processResponseNext(buffer, session)
    }

    @CallSuper
    override fun processResponseFinishedFirst(session: TcpSession) {
        processResponseFinishedNext(session)
    }

    private var requestReflux: Pair<ByteBuffer, BufferDirection>? = null

    private var responseReflux: Pair<ByteBuffer, BufferDirection>? = null

    final override fun skipRequestAndReflux(target: ByteBuffer) {
        requestReflux = target to BufferDirection.ProxyClient
    }

    final override fun skipResponseAndReflux(target: ByteBuffer) {
        responseReflux = target to BufferDirection.RemoteServer
    }

    final override fun processRequestResult(): Pair<ByteBuffer, BufferDirection>? {
        return requestReflux
    }

    final override fun processResponseResult(): Pair<ByteBuffer, BufferDirection>? {
        return responseReflux
    }
}
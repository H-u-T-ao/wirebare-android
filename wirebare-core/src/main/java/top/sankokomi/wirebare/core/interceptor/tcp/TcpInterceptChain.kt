package top.sankokomi.wirebare.core.interceptor.tcp

import androidx.annotation.CallSuper
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

    @CallSuper
    internal open fun processRequest(buffer: ByteBuffer, session: TcpSession) {
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
        processRequestNext(buffer, session)
    }

    @CallSuper
    internal open fun processRequestFinished(session: TcpSession) {
        processRequestFinishedNext(session)
        curReqRspMap.remove(session)
    }

    @CallSuper
    internal open fun processResponse(buffer: ByteBuffer, session: TcpSession) {
        processResponseNext(buffer, session)
    }

    @CallSuper
    internal open fun processResponseFinished(session: TcpSession) {
        processResponseFinishedNext(session)
    }

}
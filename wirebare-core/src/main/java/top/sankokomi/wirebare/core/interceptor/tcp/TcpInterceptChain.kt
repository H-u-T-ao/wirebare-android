package top.sankokomi.wirebare.core.interceptor.tcp

import androidx.annotation.CallSuper
import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.InterceptorChain
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

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
                    destinationAddress = session.destinationAddress.stringIp
                    destinationPort = session.destinationPort.port
                }
                it.second.run {
                    sourcePort = session.sourcePort.port
                    destinationAddress = session.destinationAddress.stringIp
                    destinationPort = session.destinationPort.port
                }
            }
        }
    }

    private var bufferResults = LinkedBlockingQueue<Pair<ByteBuffer, BufferDirection>>()

    @CallSuper
    override fun processRequestFirst(buffer: ByteBuffer, session: TcpSession) {
        bufferResults.clear()
        bufferResults.offer(buffer to BufferDirection.RemoteServer)
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
        bufferResults.clear()
        bufferResults.offer(buffer to BufferDirection.ProxyClient)
        processResponseNext(buffer, session)
    }

    @CallSuper
    override fun processResponseFinishedFirst(session: TcpSession) {
        processResponseFinishedNext(session)
    }

    final override fun processRequestFinial(target: ByteBuffer) {
        bufferResults.offer(target to BufferDirection.RemoteServer)
    }

    final override fun processResponseFinial(target: ByteBuffer) {
        bufferResults.offer(target to BufferDirection.ProxyClient)
    }

    override fun skipOriginBuffer() {
        bufferResults.poll()
    }

    final override fun processExtraBuffer(target: ByteBuffer, direction: BufferDirection) {
        bufferResults.offer(target to direction)
    }

    final override fun processResults(): Queue<Pair<ByteBuffer, BufferDirection>> {
        return bufferResults
    }
}
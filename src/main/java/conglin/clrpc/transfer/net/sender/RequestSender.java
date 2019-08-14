package conglin.clrpc.transfer.net.sender;

import conglin.clrpc.common.exception.NoSuchServerException;

import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.message.BasicRequest;

public interface RequestSender extends Runnable{

    /**
     * 初始化函数
     * @param serviceHandler
     * @param clientTransfer
     */
    void init(ClientServiceHandler serviceHandler, ClientTransfer clientTransfer);

    /**
     * 发送请求
     * @param request
     * @return
     */
    RpcFuture sendRequest(BasicRequest request);

    /**
     * 发送请求
     * 指定远端地址
     * @param remoteAddress
     * @param request
     * @return
     * @throws NoSuchServerException
     */
    RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchServerException;

    /**
     * 该方法仅用于未收到请求后的重试
     * @param future
     * @return
     * @throws NoSuchServerException
     */
    void sendRequest(RpcFuture future) throws NoSuchServerException;

    /**
     * 关闭发送器
     */
    void stop();
}
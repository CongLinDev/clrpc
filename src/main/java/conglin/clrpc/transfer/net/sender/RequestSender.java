package conglin.clrpc.transfer.net.sender;

import conglin.clrpc.common.exception.NoSuchServerException;
import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.message.BasicRequest;

public interface RequestSender extends Runnable{

    /**
     * 初始化函数
     * @param serviceHandler
     * @param clientTransfer
     */
    public void init(ClientServiceHandler serviceHandler, ClientTransfer clientTransfer);

    /**
     * 发送请求
     * @param request
     * @return
     */
    public RpcFuture sendRequest(BasicRequest request);

    /**
     * 发送请求
     * 指定远端地址
     * @param remoteAddress
     * @param request
     * @return
     * @throws NoSuchServerException
     */
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchServerException;

    /**
     * 关闭发送器
     */
    public void stop();
}
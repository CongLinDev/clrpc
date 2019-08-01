package conglin.clrpc.transfer.net.sender;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.message.BasicRequest;

public interface RequestSender extends Runnable{

    /**
     * 初始化函数
     * @param clientTransfer
     */
    public void init(ClientTransfer clientTransfer);

    /**
     * 发送请求
     * @param request
     * @return
     */
    public RpcFuture sendRequest(BasicRequest request);
}
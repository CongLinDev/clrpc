package conglin.clrpc.transfer.net.sender;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.message.BasicRequest;

public interface RequestSender extends Runnable{

    public void init(ClientTransfer clientTransfer);

    public RpcFuture sendRequest(BasicRequest request);
}
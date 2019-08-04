package conglin.clrpc.transfer.net.sender;

import org.apache.zookeeper.CreateMode;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.common.util.zookeeper.ZooKeeperUtils;
import conglin.clrpc.transfer.net.message.BasicRequest;

/**
 * 针对某一服务的有序请求发送器
 * 对于某一服务的任意请求生成唯一的请求ID
 */
public class SequetialServiceRequestSender extends SequetialRequestSender {
    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        String requestId = generateRequestId(request.getServiceName());
        request.setRequestId(requestId);

        RpcFuture future = new RpcFuture(request);
        super.serviceHandler.putFuture(requestId, future);
        sendRequestCore(request);
        return future;
    }

    @Override
    protected String generateRequestId(String serviceName) {
        if (super.keeper != null) {
            String sequetialNode = rootPath + "/service/" + serviceName + "/request/id";
            String sequetialId = ZooKeeperUtils.createNode(super.keeper, sequetialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            return sequetialId.substring(sequetialId.lastIndexOf('/') + 3, sequetialId.length());
        }
        return super.generateRequestId(null);
    }

}
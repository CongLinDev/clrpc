package conglin.clrpc.transfer.net.sender;

import org.apache.zookeeper.CreateMode;

import conglin.clrpc.common.exception.NoSuchServerException;
import conglin.clrpc.common.util.zookeeper.ZooKeeperUtils;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.net.message.BasicRequest;

/**
 * 针对某一服务的有序请求发送器
 * 对于某一服务的任意请求生成唯一的请求ID
 */
public class SequetialServiceRequestSender extends SequetialRequestSender {
    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        RpcFuture future = generateFuture(this::generateRequestId, request);
        String addr = sendRequestCore(request);
        future.setRemoteAddress(addr);
        return future;
    }

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchServerException {
        RpcFuture future = generateFuture(this::generateRequestId, request);
        sendRequestCore(remoteAddress, request);
        return future;
    }

    @Override
    protected Long generateRequestId(String serviceName) {
        if (super.keeper != null) {
            String sequetialNode = rootPath + "/service/" + serviceName + "/request/id";
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, sequetialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        return super.generateRequestId(serviceName);
    }

}
package conglin.clrpc.transfer.sender;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.common.util.zookeeper.ZooKeeperUtils;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;

/**
 * 有序的请求发送器
 * 对于任意请求生成唯一的请求ID
 */
public class SequetialRequestSender extends BasicRequestSender {

    private static final Logger log = LoggerFactory.getLogger(SequetialRequestSender.class);

    protected final String rootPath; //zookeeper根地址
    protected final ZooKeeper keeper;

    public SequetialRequestSender(){
        String registryAddress = ConfigParser.getOrDefault("zookeeper.registry.address", "localhost:2181");
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);

        String path = ConfigParser.getOrDefault("zookeeper.registry.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length()-1) : path;//去除最后一个 /
    }


    @Override
    protected Long generateRequestId(String serviceName) {
        if (keeper != null) {
            String sequetialNode = rootPath + "/request/id";
            String nodeSequetialId = ZooKeeperUtils.createNode(keeper, sequetialNode, "", CreateMode.EPHEMERAL_SEQUENTIAL);
            String id = nodeSequetialId.substring(nodeSequetialId.lastIndexOf('/') + 3, nodeSequetialId.length());
            return Long.parseLong(id);
        }
        return super.generateRequestId(serviceName);
    }

    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        RpcFuture future = generateFuture(this::generateRequestId, request);
        String addr = sendRequestCore(request);
        future.setRemoteAddress(addr);
        return future;
    }

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException {
        RpcFuture future = generateFuture(this::generateRequestId, request);
        sendRequestCore(remoteAddress, request);
        return future;
    }

    @Override
    public void stop() {
        if(keeper != null){
            try {
                keeper.close();
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }
}
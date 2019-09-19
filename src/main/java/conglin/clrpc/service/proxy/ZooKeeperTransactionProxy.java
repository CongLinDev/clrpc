package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.common.util.ZooKeeperUtils;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.TransactionRequest;
import conglin.clrpc.transfer.sender.RequestSender;

/**
 * 使用 ZooKeeper 控制分布式事务
 * 注意，该类是线程不安全的
 */
public class ZooKeeperTransactionProxy extends AbstractProxy implements TransactionProxy {

    protected List<TransactionRequest> requests; // 每一个代理在保证线程安全的情况下可以重复使用多次

    protected final ZooKeeper keeper;
    protected final String rootPath;

    protected Long currentTransactionId;

    public ZooKeeperTransactionProxy(RequestSender sender, IdentifierGenerator identifierGenerator){
        super(null, sender, identifierGenerator);

        requests = new ArrayList<>();
        
        String path = ConfigParser.getOrDefault("zookeeper.atomicity.root-path", "/clrpc") ;
        rootPath = path.endsWith("/") ? path + "atomic/transaction" : path + "/atomic/transaction";

        String atomicityAddress = ConfigParser.getOrDefault("zookeeper.atomicity.address", "localhost:2181");
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(atomicityAddress, sessionTimeout);
    }

    @Override
    public TransactionProxy begin() {
        this.requests.clear();
        clearWithZooKeeper(currentTransactionId);
        currentTransactionId = identifierGenerator.generateIdentifier();
        beginWithZooKeeper(currentTransactionId);

        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, String method, Object... args) {
        TransactionRequest request = new TransactionRequest();

        request.setRequestId(currentTransactionId);
        request.setServiceName(serviceName);
        request.setMethodName(method);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));
        request.setSerialNumber(requests.size());

        requests.add(request);
        callWithZooKeeper(currentTransactionId, request.getSerialNumber());
        handleRequest(request);
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, Method method, Object... args) {
        TransactionRequest request = new TransactionRequest();
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setSerialNumber(requests.size());

        requests.add(request);
        callWithZooKeeper(currentTransactionId, request.getSerialNumber());
        handleRequest(request);
        return this;
    }

    @Override
    public RpcFuture commit() {
        return null;
    }

	@Override
	public boolean rollback() {
		throw new UnsupportedOperationException();
    }
    
    /**
     * 处理请求
     * @param request
     * @return
     */
    protected void handleRequest(TransactionRequest request){
        
    }

    





    // 事务状态
    private static final String PREPARE = "PREPARE";
    // private static final String DONE = "DONE";

    protected void beginWithZooKeeper(Long requestId){
        // 创建临时节点
        ZooKeeperUtils.createNode(keeper, rootPath + "/" + requestId.toString(), PREPARE, CreateMode.EPHEMERAL);
    }

    protected void clearWithZooKeeper(Long requestId){
        if(requestId == null) return;
        ZooKeeperUtils.deleteNode(keeper, rootPath + "/" + requestId);
    }

    protected void callWithZooKeeper(Long requestId, Integer serialNumber){
        // 创建临时子节点
        ZooKeeperUtils.createNode(keeper, 
            rootPath + "/" + requestId.toString() + "/" + serialNumber.toString(),
            PREPARE,
            CreateMode.EPHEMERAL);
    }
}
package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.TransactionRequest;
import conglin.clrpc.transfer.sender.RequestSender;

/**
 * 使用 ZooKeeper 控制分布式事务
 * 注意，该类是线程不安全的
 */
public class ZooKeeperTransactionProxy extends AbstractProxy implements TransactionProxy {

    protected List<TransactionRequest> requests; // 每一个代理在保证线程安全的情况下可以重复使用多次

    protected Long currentTransactionId;

    protected final ZooKeeperTransactionHelper helper;

    public ZooKeeperTransactionProxy(RequestSender sender, IdentifierGenerator identifierGenerator){
        super(null, sender, identifierGenerator);
        requests = new ArrayList<>();
        helper = new ZooKeeperTransactionHelper();
    }

    @Override
    public TransactionProxy begin() {
        this.requests.clear();
        helper.clearWithZooKeeper(currentTransactionId);
        currentTransactionId = identifierGenerator.generateIdentifier();
        helper.beginWithZooKeeper(currentTransactionId);

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
        helper.callWithZooKeeper(currentTransactionId, request.getSerialNumber());
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
        helper.callWithZooKeeper(currentTransactionId, request.getSerialNumber());
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
        // sender.sendRequest(request);
    }

}


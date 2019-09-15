package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.ZooKeeper;

import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.common.util.ZooKeeperUtils;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.TransactionRequest;

/**
 * 使用 ZooKeeper 控制分布式事务
 * 注意，该类是线程不安全的
 */
public class ZooKeeperTransactionProxy implements TransactionProxy {

    protected List<TransactionRequest> requests;

    protected ZooKeeper keeper;

    public ZooKeeperTransactionProxy(){
        requests = new ArrayList<>();

        String atomicityAddress = ConfigParser.getOrDefault("zookeeper.atomicity.address", "localhost:2181");
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(atomicityAddress, sessionTimeout);
    }

    @Override
    public TransactionProxy begin() {
        this.requests.clear();
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, String method, Object... args) {
        TransactionRequest request = new TransactionRequest();
        request.setServiceName(serviceName);
        request.setMethodName(method);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));
        request.setSerialNumber(requests.size());

        requests.add(request);
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

    protected Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }

}
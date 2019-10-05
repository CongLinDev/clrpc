package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.atomic.TransactionHelper;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.future.TransactionFuture;
import conglin.clrpc.transfer.message.TransactionRequest;
import conglin.clrpc.transfer.sender.RequestSender;

/**
 * 使用 ZooKeeper 控制分布式事务
 * 注意，该类是线程不安全的
 */
public class ZooKeeperTransactionProxy extends AbstractProxy implements TransactionProxy {

    protected long currentTransactionId;

    protected final TransactionHelper helper;

    protected TransactionFuture future;

    public ZooKeeperTransactionProxy(RequestSender sender, IdentifierGenerator identifierGenerator){
        super(null, sender, identifierGenerator);
        helper = new ZooKeeperTransactionHelper();
    }

    @Override
    public TransactionProxy begin() throws TransactionException {
        future = new TransactionFuture();
        
        helper.clear(currentTransactionId); // 清除上个事务的记录
        currentTransactionId = identifierGenerator.generate(); // 生成一个新的ID
        helper.begin(currentTransactionId); // 开启事务
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest();

        request.setRequestId(currentTransactionId);
        request.setServiceName(serviceName);
        request.setMethodName(method);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));
        request.setSerialNumber(future.size() + 1); // 由future进行控制序列号
        handleRequest(request);
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, Method method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest();
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setSerialNumber(future.size() + 1); // 由future进行控制序列号
        handleRequest(request);
        return this;
    }

    @Override
    public RpcFuture commit() throws TransactionException {
        helper.commit(currentTransactionId);
        return future;
    }

	@Override
	public void rollback() throws TransactionException {
		helper.rollback(currentTransactionId);
    }
    
    /**
     * 处理请求
     * @param request
     * @throws TransactionException
     */
    protected void handleRequest(TransactionRequest request) throws TransactionException {
        RpcFuture f = sender.sendRequest(request);
        if(future.combine(f)){
            helper.prepare(currentTransactionId, request.getSerialNumber());
        }
    }

    /**
     * 销毁
     */
    public void destroy(){
        helper.destroy();
    }

}


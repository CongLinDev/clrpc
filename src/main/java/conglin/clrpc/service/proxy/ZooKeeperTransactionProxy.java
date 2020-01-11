package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.atomic.TransactionHelper;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.future.TransactionFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.TransactionRequest;

/**
 * 使用 ZooKeeper 控制分布式事务 注意，该类是线程不安全的
 * 
 * 在某一时刻最多只能保证一个事务
 */
public class ZooKeeperTransactionProxy extends AbstractProxy implements TransactionProxy, Available {

    protected long currentTransactionId;

    protected final TransactionHelper helper;

    protected TransactionFuture future;

    public ZooKeeperTransactionProxy(RequestSender sender, IdentifierGenerator identifierGenerator,
            PropertyConfigurer configurer) {
        super(sender, identifierGenerator);
        helper = new ZooKeeperTransactionHelper(configurer);
    }

    @Override
    public TransactionProxy begin() throws TransactionException {
        future = new TransactionFuture();

        currentTransactionId = identifierGenerator.generate(); // 生成一个新的ID
        helper.begin(currentTransactionId); // 开启事务
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest(currentTransactionId);

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
        TransactionRequest request = new TransactionRequest(currentTransactionId);

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
        RpcFuture f = future;
        future = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public void abort() throws TransactionException {
        if (future.isDone())
            throw new TransactionException("Transaction request has commited. Can not abort.");
        helper.abort(currentTransactionId);
        future = null;
    }

    /**
     * 处理原子请求
     * 
     * @param request
     * @throws TransactionException
     */
    protected void handleRequest(TransactionRequest request) throws TransactionException {
        RpcFuture f = sender.sendRequest(request);
        if (future.combine(f)) {
            helper.prepare(currentTransactionId, request.getSerialNumber());
        } else {
            throw new TransactionException("Request added failed. " + request);
        }
    }

    @Override
    public boolean isAvailable() {
        return future == null;
    }
}

package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperTransactionProxy.class);

    protected long currentTransactionId;
    protected boolean serial; // 是否顺序执行

    protected final TransactionHelper helper;

    protected TransactionFuture future;

    public ZooKeeperTransactionProxy(RequestSender sender, IdentifierGenerator identifierGenerator,
            PropertyConfigurer configurer) {
        super(sender, identifierGenerator);
        helper = new ZooKeeperTransactionHelper(configurer);
    }

    @Override
    public TransactionProxy begin() throws TransactionException {
        return begin(false);
    }

    @Override
    public TransactionProxy begin(boolean serial) throws TransactionException {
        this.currentTransactionId = identifierGenerator.generate() << 32; // 生成一个新的ID
        this.serial = serial;
        this.future = new TransactionFuture(currentTransactionId);
        LOGGER.debug("Transaction id={} will begin.", currentTransactionId);
        helper.begin(currentTransactionId); // 开启事务
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest(currentTransactionId, future.size() + 1, serial);
        request.setServiceName(serviceName);
        request.setMethodName(method);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));

        handleRequest(request);
        return this;
    }

    @Override
    public TransactionProxy call(String serviceName, Method method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest(currentTransactionId, future.size() + 1);
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        handleRequest(request);
        return this;
    }

    @Override
    public RpcFuture commit() throws TransactionException {
        LOGGER.debug("Transaction id={} will commit.", currentTransactionId);
        helper.commit(currentTransactionId);
        RpcFuture f = future;
        future = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public void abort() throws TransactionException {
        if (future.isDone())
            throw new TransactionException("Transaction request has commited. Can not abort.");
        LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
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
        helper.prepare(currentTransactionId, request.getSerialId());
        RpcFuture f = sender.sendRequest(request);
        if (!future.combine(f)) {
            throw new TransactionException("Request added failed. " + request);
        }
    }

    @Override
    public boolean isAvailable() {
        return future == null;
    }
}

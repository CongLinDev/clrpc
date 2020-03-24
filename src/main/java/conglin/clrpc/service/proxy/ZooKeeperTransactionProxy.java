package conglin.clrpc.service.proxy;

import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.future.TransactionFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.TransactionRequest;
import conglin.clrpc.zookeeper.util.ZooKeeperTransactionHelper;

/**
 * 使用 ZooKeeper 控制分布式事务 注意，该类是线程不安全的
 * 
 * 在某一时段只能操作一个事务，如果使用者不确定代理是否可用，可调用
 * {@link ZooKeeperTransactionProxy#isAvailable()} 查看
 */
public class ZooKeeperTransactionProxy extends CommonProxy implements TransactionProxy, Available {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperTransactionProxy.class);

    // ID生成器
    protected final IdentifierGenerator identifierGenerator;

    protected long currentTransactionId;
    protected boolean serial; // 是否顺序执行

    protected final TransactionHelper helper;

    protected TransactionFuture transactionFuture;

    public ZooKeeperTransactionProxy(RequestSender sender, IdentifierGenerator identifierGenerator,
            PropertyConfigurer configurer) {
        super(sender);
        this.identifierGenerator = identifierGenerator;
        helper = new ZooKeeperTransactionHelper(configurer);
    }

    @Override
    public void begin(boolean serial) throws TransactionException {
        if(isTransaction()) {
            throw new TransactionException("Transaction has been begined with this proxy");
        }
        this.currentTransactionId = identifierGenerator.generate() << 32; // 生成一个新的ID
        this.serial = serial;
        this.transactionFuture = new TransactionFuture(currentTransactionId);
        LOGGER.debug("Transaction id={} will begin.", currentTransactionId);
        helper.begin(currentTransactionId); // 开启事务
    }

    @Override
    public RpcFuture call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest(currentTransactionId, transactionFuture.size() + 1, serial);
        request.setServiceName(serviceName);
        request.setMethodName(method);
        request.setParameters(args);

        return call(request);
    }

    @Override
    public RpcFuture commit() throws TransactionException {
        if(!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        LOGGER.debug("Transaction id={} will commit.", currentTransactionId);
        helper.commit(currentTransactionId);
        RpcFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public void abort() throws TransactionException {
        if(!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        if (transactionFuture.isDone())
            throw new TransactionException("Transaction request has commited. Can not abort.");
        LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
        helper.abort(currentTransactionId);
        transactionFuture = null;
    }

    @Override
    public RpcFuture call(TransactionRequest request) throws TransactionException {
        helper.prepare(currentTransactionId, request.getSerialId());
        RpcFuture f = super.call(request);
        if (!transactionFuture.combine(f)) {
            throw new TransactionException("Atomic request added failed. " + request);
        }
        return f;
    }

    @Override
    public boolean isAvailable() {
        return transactionFuture == null;
    }

    /**
     * 当前是否在进行着事务
     * 
     * @return
     */
    protected boolean isTransaction() {
        return transactionFuture != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T subscribeAsync(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { interfaceClass },
                new InnerAsyncObjectProxy(interfaceClass));
    }

    class InnerAsyncObjectProxy extends AsyncObjectProxy {

        public InnerAsyncObjectProxy(Class<?> interfaceClass) {
            super(interfaceClass, ZooKeeperTransactionProxy.this.requestSender(),
                    ZooKeeperTransactionProxy.this.identifierGenerator);
        }

        @Override
        public RpcFuture call(String serviceName, String methodName, Object... args) {
            if(isTransaction())
                return ZooKeeperTransactionProxy.this.call(serviceName, methodName, args);
            return super.call(serviceName, methodName, args);
        }

    }
}

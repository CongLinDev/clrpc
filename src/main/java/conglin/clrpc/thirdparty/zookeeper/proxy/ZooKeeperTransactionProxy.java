package conglin.clrpc.thirdparty.zookeeper.proxy;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.CommonProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.future.TransactionFuture;
import conglin.clrpc.transport.message.TransactionRequest;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;

/**
 * 使用 ZooKeeper 控制分布式事务 注意，该类是线程不安全的
 * 
 * 在某一时段只能操作一个事务，如果使用者不确定代理是否可用，可调用 {@link #isAvailable()} 查看
 */
public class ZooKeeperTransactionProxy extends CommonProxy implements TransactionProxy, Available {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperTransactionProxy.class);

    // ID生成器
    protected final IdentifierGenerator identifierGenerator;

    protected long currentTransactionId;

    protected final TransactionHelper helper;

    protected TransactionFuture transactionFuture;

    public ZooKeeperTransactionProxy(RpcContext context) {
        super(context.getWith(RpcContextEnum.REQUEST_SENDER));
        this.identifierGenerator = context.getWith(RpcContextEnum.IDENTIFIER_GENERATOR);
        PropertyConfigurer c = context.getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        helper = new ZooKeeperTransactionHelper(new UrlScheme(c.get("atomicity.url", String.class)));
    }

    @Override
    public void begin() throws TransactionException {
        if (isTransaction()) {
            throw new TransactionException("Transaction has been begin with this proxy");
        }
        this.currentTransactionId = identifierGenerator.generate(); // 生成一个新的ID
        this.transactionFuture = new TransactionFuture(currentTransactionId);
        LOGGER.debug("Transaction id={} will begin.", currentTransactionId);
        helper.begin(currentTransactionId); // 开启事务
    }

    @Override
    public RpcFuture call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequest request = new TransactionRequest(currentTransactionId, transactionFuture.size() + 1, serviceName, method, args);
        return call(request);
    }

    @Override
    public RpcFuture commit() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        if (helper.check(currentTransactionId)) { // 如果可以进行提交
            helper.commit(currentTransactionId);
            LOGGER.debug("Transaction id={} will commit.", currentTransactionId);
        } else {
            helper.abort(currentTransactionId);
            LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
        }

        RpcFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public RpcFuture commit(long timeout, TimeUnit unit) throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        if (helper.check(currentTransactionId, timeout, unit)) { // 如果可以进行提交
            helper.commit(currentTransactionId);
            LOGGER.debug("Transaction id={} will commit.", currentTransactionId);
        } else {
            helper.abort(currentTransactionId);
            LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
        }

        RpcFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public RpcFuture abort() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        if (transactionFuture.isDone())
            throw new TransactionException("Transaction request has committed. Can not abort.");
        LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
        helper.abort(currentTransactionId);

        RpcFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public RpcFuture call(TransactionRequest request) throws TransactionException {
        helper.prepare(currentTransactionId, request.serialId());
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
    public <T> T proxy(ServiceInterface<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() }, new InnerAsyncObjectProxy(serviceInterface));
    }

    class InnerAsyncObjectProxy extends AsyncObjectProxy {

        public InnerAsyncObjectProxy(ServiceInterface<?> serviceInterface) {
            super(serviceInterface, ZooKeeperTransactionProxy.this.requestSender(),
                    ZooKeeperTransactionProxy.this.identifierGenerator);
        }

        @Override
        public RpcFuture call(String serviceName, String methodName, Object... args) {
            if (isTransaction())
                return ZooKeeperTransactionProxy.this.call(serviceName, methodName, args);
            return super.call(serviceName, methodName, args);
        }

    }
}

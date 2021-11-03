package conglin.clrpc.thirdparty.zookeeper.proxy;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.*;
import conglin.clrpc.global.GlobalMessageManager;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.SimpleProxy;
import conglin.clrpc.service.util.ObjectAssemblyUtils;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;
import conglin.clrpc.transport.message.RequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 使用 ZooKeeper 控制分布式事务 注意，该类是线程不安全的
 * <p>
 * 在某一时段只能操作一个事务，如果使用者不确定代理是否可用，可调用 {@link #isAvailable()} 查看
 */
public class ZooKeeperTransactionProxy extends SimpleProxy implements TransactionProxy, Available, Destroyable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperTransactionProxy.class);

    // ID生成器
    protected IdentifierGenerator identifierGenerator;

    protected long currentTransactionId;

    protected ZooKeeperTransactionHelper helper;

    protected TransactionFuture transactionFuture;

    @Override
    public void init() {
        super.init();
        this.identifierGenerator = getContext().getWith(RpcContextEnum.IDENTIFIER_GENERATOR);
        PropertyConfigurer c = getContext().getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        helper = new ZooKeeperTransactionHelper(new UrlScheme(c.get("extension.atomicity.url", String.class)));
        GlobalMessageManager.manager().setMessageClass(TransactionRequest.MESSAGE_TYPE, TransactionRequest.class);
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

        if (transactionFuture.isDone()) {
            if (transactionFuture.isError()) {
                helper.abort(currentTransactionId);
            } else {
                helper.commit(currentTransactionId);
            }
        } else {
            try {
                transactionFuture.get();
                return commit();
            } catch (Exception e) {
                helper.abort(currentTransactionId);
            }
        }

        RpcFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public RpcFuture commit(long timeout, TimeUnit unit) throws TransactionException, TimeoutException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }

        if (transactionFuture.isDone()) {
            if (transactionFuture.isError()) {
                helper.abort(currentTransactionId);
            } else {
                helper.commit(currentTransactionId);
            }
        } else {
            try {
                transactionFuture.get(timeout, unit);
                return commit(timeout, unit);
            } catch (InterruptedException | ExecutionException e) {
                helper.abort(currentTransactionId);
            }
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
        LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
        helper.abort(currentTransactionId);

        RpcFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public RpcFuture call(TransactionRequest request) throws TransactionException {
        RequestWrapper wrapper = new RequestWrapper();
        wrapper.setRequest(request);
        wrapper.setBeforeSendRequest(instance -> {
            TransactionRequest transactionRequest = (TransactionRequest) wrapper.getRequest();
            try {
                helper.prepare(transactionRequest.transactionId(), transactionRequest.serialId(), instance.address());
            } catch (TransactionException e) {
                LOGGER.error("Transaction message(transactionId={}, serialId={}) prepare failed. {}", transactionRequest.transactionId(), transactionRequest.serialId(), e.getMessage());
            }
        });
        RpcFuture f = super.call(wrapper);
        if (!transactionFuture.combine(f)) {
            throw new TransactionException("Atomic request added failed. " + request);
        }
        return f;
    }

    @Override
    public boolean isAvailable() {
        return transactionFuture == null;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        helper.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return helper.isDestroyed();
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
        InnerAsyncObjectProxy proxy = new InnerAsyncObjectProxy(serviceInterface);
        ObjectAssemblyUtils.assemble(proxy, getContext());
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{serviceInterface.interfaceClass()}, proxy);
    }

    class InnerAsyncObjectProxy extends AsyncObjectProxy {

        public InnerAsyncObjectProxy(ServiceInterface<?> serviceInterface) {
            super(serviceInterface);
        }

        @Override
        public RpcFuture call(String serviceName, String methodName, Object... args) {
            if (isTransaction()) {
                try {
                    return ZooKeeperTransactionProxy.this.call(serviceName, methodName, args);
                } catch (TransactionException e) {
                    return super.call(serviceName, methodName, args);
                }
            }
            return super.call(serviceName, methodName, args);
        }
    }
}

package conglin.clrpc.thirdparty.zookeeper.proxy;

import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.TransactionFuture;
import conglin.clrpc.extension.transaction.TransactionProxy;
import conglin.clrpc.extension.transaction.TransactionRequestPayload;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.SimpleProxy;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperTransactionHelper;
import conglin.clrpc.transport.message.RequestWrapper;
import conglin.clrpc.transport.protocol.ProtocolDefinition;

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
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        helper = new ZooKeeperTransactionHelper(new UrlScheme(properties.getProperty("extension.atomicity.url")));
        ProtocolDefinition protocolDefinition = getContext().getWith(ComponentContextEnum.PROTOCOL_DEFINITION);
        protocolDefinition.setPayloadType(TransactionRequestPayload.PAYLOAD_TYPE, TransactionRequestPayload.class);
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
    public InvocationFuture call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequestPayload request = new TransactionRequestPayload(currentTransactionId,
                nextSerialId(), serviceName, method, args);
        return call(null, request);
    }

    @Override
    public InvocationFuture call(InstanceCondition instanceCondition, String serviceName, String method, Object... args)
            throws TransactionException {
        TransactionRequestPayload request = new TransactionRequestPayload(currentTransactionId,
                nextSerialId(), serviceName, method, args);
        return call(instanceCondition, request);
    }

    /**
     * 发送事务内部的一条原子性请求
     * 
     * @param instanceCondition instance condition
     * @param request           请求
     * @return sub future
     * @throws TransactionException
     */
    public InvocationFuture call(InstanceCondition instanceCondition, TransactionRequestPayload request)
            throws TransactionException {
        RequestWrapper wrapper = new RequestWrapper();
        wrapper.setRequest(request);
        wrapper.setInstanceCondition(instanceCondition);
        wrapper.setInstanceConsumer(instance -> {
            TransactionRequestPayload transactionRequest = (TransactionRequestPayload) wrapper.getRequest();
            try {
                helper.prepare(transactionRequest.transactionId(), transactionRequest.serialId(), instance.address());
            } catch (TransactionException e) {
                LOGGER.error("Transaction message(transactionId={}, serialId={}) prepare failed. {}",
                        transactionRequest.transactionId(), transactionRequest.serialId(), e.getMessage());
            }
        });
        InvocationFuture f = super.call(wrapper);
        if (!transactionFuture.combine(f)) {
            throw new TransactionException("Atomic request added failed. " + request);
        }
        return f;
    }

    @Override
    public InvocationFuture call(RequestWrapper requestWrapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InvocationFuture commit() throws TransactionException {
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

        InvocationFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public InvocationFuture commit(long timeout, TimeUnit unit) throws TransactionException, TimeoutException {
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

        InvocationFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
        return f;
    }

    @Override
    public InvocationFuture abort() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        LOGGER.debug("Transaction id={} will abort.", currentTransactionId);
        helper.abort(currentTransactionId);

        InvocationFuture f = transactionFuture;
        transactionFuture = null; // 提交后该代理对象可以进行重用
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

    /**
     * 下一个序列号
     * 
     * @return
     */
    protected int nextSerialId() {
        return transactionFuture.size() + 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T proxy(ServiceInterface<T> serviceInterface) {
        InnerAsyncObjectProxy proxy = new InnerAsyncObjectProxy(serviceInterface);
        ObjectLifecycleUtils.assemble(proxy, getContext());
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() }, proxy);
    }

    class InnerAsyncObjectProxy extends AsyncObjectProxy {

        public InnerAsyncObjectProxy(ServiceInterface<?> serviceInterface) {
            super(serviceInterface);
        }

        @Override
        public InvocationFuture call(String serviceName, String methodName, Object... args) {
            if (isTransaction()) {
                try {
                    return ZooKeeperTransactionProxy.this.call(instanceCondition(), serviceName, methodName, args);
                } catch (TransactionException e) {
                    return super.call(serviceName, methodName, args);
                }
            } else {
                return super.call(serviceName, methodName, args);
            }
        }
    }
}

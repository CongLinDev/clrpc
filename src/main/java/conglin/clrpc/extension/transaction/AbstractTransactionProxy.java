package conglin.clrpc.extension.transaction;

import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Available;
import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.SimpleProxy;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.protocol.ProtocolDefinition;

/**
 * 分布式事务代理 注意，该类是线程不安全的
 * <p>
 * 在某一时段只能操作一个事务，如果使用者不确定代理是否可用，可调用 {@link #isAvailable()} 查看
 */
abstract public class AbstractTransactionProxy extends SimpleProxy implements TransactionProxy, Available, Destroyable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransactionProxy.class);

    // ID生成器
    protected IdentifierGenerator identifierGenerator;

    protected TransactionHelper helper;

    protected TransactionInvocationContext transactionInvocationContext;

    /**
     * 获取helper
     * 
     * @param urlScheme
     * @return helper
     */
    abstract protected TransactionHelper getTransactionHelper(UrlScheme urlScheme);

    @Override
    public void init() {
        super.init();
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        helper = getTransactionHelper(new UrlScheme(properties.getProperty("extension.atomicity.url")));
        ProtocolDefinition protocolDefinition = getContext().getWith(ComponentContextEnum.PROTOCOL_DEFINITION);
        protocolDefinition.setPayloadType(TransactionRequestPayload.PAYLOAD_TYPE, TransactionRequestPayload.class);
    }

    @Override
    public void begin() throws TransactionException {
        if (isTransaction()) {
            throw new TransactionException("Transaction has been begin with this proxy");
        }
        long currentTransactionId = identifierGenerator.generate(); // 生成一个新的ID
        transactionInvocationContext = new TransactionInvocationContext(currentTransactionId);
        LOGGER.debug("Transaction id={} will begin.", currentTransactionId);
        helper.begin(currentTransactionId); // 开启事务
    }

    @Override
    public InvocationContext call(String serviceName, String method, Object... args) throws TransactionException {
        TransactionRequestPayload request = new TransactionRequestPayload(transactionInvocationContext.getIdentifier(),
                nextSerialId(), serviceName, method, args);
        return call(null, request);
    }

    @Override
    public InvocationContext call(InstanceCondition instanceCondition, String serviceName, String method, Object... args)
            throws TransactionException {
        TransactionRequestPayload request = new TransactionRequestPayload(transactionInvocationContext.getIdentifier(),
                nextSerialId(), serviceName, method, args);
        return call(instanceCondition, request);
    }

    /**
     * 发送事务内部的一条原子性请求
     * 
     * @param instanceCondition instance condition
     * @param request           请求
     * @return sub InvocationContext
     * @throws TransactionException
     */
    public InvocationContext call(InstanceCondition instanceCondition, TransactionRequestPayload request)
            throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        InvocationContext invocationContext = new InvocationContext();
        invocationContext.setRequest(request);
        invocationContext.setInstanceCondition(instanceCondition);
        invocationContext.setInstanceConsumer(buildInstanceConsumer(invocationContext));
        super.call(invocationContext);
        transactionInvocationContext.getFuture().combine(invocationContext.getFuture());
        transactionInvocationContext.getInvocationContextList().add(invocationContext);
        return invocationContext;
    }

    @Override
    public void call(InvocationContext invocationContext) {
        if (!isTransaction()) {
            throw new IllegalArgumentException("Transaction does not begin with this proxy");
        }
        if  (invocationContext.getRequest() instanceof TransactionRequestPayload) {
            throw new IllegalArgumentException();
        }
        invocationContext.setInstanceConsumer(buildInstanceConsumer(invocationContext));
        super.call(invocationContext);
        transactionInvocationContext.getFuture().combine(invocationContext.getFuture());
        transactionInvocationContext.getInvocationContextList().add(invocationContext);
    }

    @Override
    public TransactionInvocationContext commit() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }

        if (transactionInvocationContext.getFuture().isDone()) {
            if (transactionInvocationContext.getFuture().isError()) {
                helper.abort(transactionInvocationContext.getIdentifier());
                transactionInvocationContext.abort();
            } else {
                helper.commit(transactionInvocationContext.getIdentifier());
                transactionInvocationContext.commit();
            }
        } else {
            try {
                transactionInvocationContext.getFuture().get();
                return commit();
            } catch (Exception e) {
                helper.abort(transactionInvocationContext.getIdentifier());
                transactionInvocationContext.abort();
            }
        }

        TransactionInvocationContext c = transactionInvocationContext;
        transactionInvocationContext = null; // 提交后该代理对象可以进行重用
        return c;
    }

    @Override
    public TransactionInvocationContext commit(long timeout, TimeUnit unit) throws TransactionException, TimeoutException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }

        if (transactionInvocationContext.getFuture().isDone()) {
            if (transactionInvocationContext.getFuture().isError()) {
                helper.abort(transactionInvocationContext.getIdentifier());
                transactionInvocationContext.abort();
            } else {
                helper.commit(transactionInvocationContext.getIdentifier());
                transactionInvocationContext.commit();
            }
        } else {
            try {
                transactionInvocationContext.getFuture().get(timeout, unit);
                return commit(timeout, unit);
            } catch (InterruptedException | ExecutionException e) {
                helper.abort(transactionInvocationContext.getIdentifier());
                transactionInvocationContext.abort();
            }
        }

        TransactionInvocationContext c = transactionInvocationContext;
        transactionInvocationContext = null; // 提交后该代理对象可以进行重用
        return c;
    }

    @Override
    public TransactionInvocationContext abort() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        LOGGER.debug("Transaction id={} will abort.", transactionInvocationContext.getIdentifier());
        helper.abort(transactionInvocationContext.getIdentifier());
        transactionInvocationContext.abort();
        TransactionInvocationContext c = transactionInvocationContext;
        transactionInvocationContext = null; // 提交后该代理对象可以进行重用
        return c;
    }

    @Override
    public boolean isAvailable() {
        return transactionInvocationContext == null;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        ObjectLifecycleUtils.destroy(helper);
    }

    @Override
    public boolean isDestroyed() {
        return ObjectLifecycleUtils.isDestroyed(helper);
    }

    /**
     * 当前是否在进行着事务
     *
     * @return
     */
    protected boolean isTransaction() {
        return transactionInvocationContext != null;
    }

    /**
     * 下一个序列号
     * 
     * @return
     */
    protected int nextSerialId() {
        return transactionInvocationContext.getFuture().size() + 1;
    }

    /**
     * 构造 InstanceConsumer
     * 
     * @param invocationContext
     * @return
     */
    protected Consumer<ServiceInstance> buildInstanceConsumer(final InvocationContext invocationContext) {
        return (instance -> {
            TransactionRequestPayload transactionRequest = (TransactionRequestPayload) invocationContext.getRequest();
            try {
                helper.prepare(transactionRequest.transactionId(), transactionRequest.serialId(), instance.id());
            } catch (TransactionException e) {
                LOGGER.error("Transaction message(transactionId={}, serialId={}) prepare failed. {}",
                        transactionRequest.transactionId(), transactionRequest.serialId(), e.getMessage());
            }
        });
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
        public InvocationContext call(String serviceName, String methodName, Object... args) {
            if (isTransaction()) {
                try {
                    return AbstractTransactionProxy.this.call(instanceCondition(), serviceName, methodName, args);
                } catch (TransactionException e) {
                    return super.call(serviceName, methodName, args);
                }
            } else {
                return super.call(serviceName, methodName, args);
            }
        }
    }
}


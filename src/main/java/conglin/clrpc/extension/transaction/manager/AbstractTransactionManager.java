package conglin.clrpc.extension.transaction.manager;

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
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.TransactionHelper;
import conglin.clrpc.extension.transaction.context.TransactionInvocationContext;
import conglin.clrpc.extension.transaction.payload.TransactionRequestPayload;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.protocol.ProtocolDefinition;

/**
 * 分布式事务管理 注意，该类是线程不安全的
 * <p>
 * 在某一时段只能操作一个事务，如果使用者不确定manager是否可用，可调用 {@link #isAvailable()} 查看
 */
abstract public class AbstractTransactionManager
        implements TransactionManager, Initializable, ComponentContextAware, Available, Destroyable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransactionManager.class);

    protected ComponentContext componentContext;
    // ID生成器
    protected IdentifierGenerator identifierGenerator;

    protected TransactionHelper helper;

    protected TransactionInvocationContext transactionInvocationContext;

    private String instanceId;

    /**
     * 获取helper
     * 
     * @param urlScheme
     * @return helper
     */
    abstract protected TransactionHelper getTransactionHelper(UrlScheme urlScheme);

    @Override
    public void init() {
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        this.instanceId = properties.getProperty("consumer.instance.id");
        if (instanceId == null) {
            throw new IllegalArgumentException("properties 'consumer.instance.id' must not be null");
        }
        helper = getTransactionHelper(new UrlScheme(properties.getProperty("extension.atomicity.url")));
        ProtocolDefinition protocolDefinition = getContext().getWith(ComponentContextEnum.PROTOCOL_DEFINITION);
        protocolDefinition.setPayloadType(TransactionRequestPayload.PAYLOAD_TYPE, TransactionRequestPayload.class);
    }

    @Override
    public ComponentContext getContext() {
        return componentContext;
    }

    @Override
    public void setContext(ComponentContext context) {
        this.componentContext = context;
    }

    @Override
    public void begin() throws TransactionException {
        if (isTransaction()) {
            throw new TransactionException("Transaction has been begin with this proxy");
        }
        long currentTransactionId = identifierGenerator.generate(); // 生成一个新的ID
        transactionInvocationContext = new TransactionInvocationContext(currentTransactionId);
        LOGGER.debug("Transaction id={} will begin.", currentTransactionId);
        helper.begin(currentTransactionId, instanceId); // 开启事务
    }

    @Override
    public TransactionInvocationContext commit() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }

        transactionInvocationContext.getFuture().combineDone();
        if (transactionInvocationContext.getFuture().isDone()) {
            if (transactionInvocationContext.getFuture().isError()) {
                helper.abort(transactionInvocationContext.getIdentifier(), instanceId);
                transactionInvocationContext.abort();
            } else {
                helper.commit(transactionInvocationContext.getIdentifier(), instanceId);
                transactionInvocationContext.commit();
            }
        } else {
            try {
                transactionInvocationContext.getFuture().get();
                return commit();
            } catch (Exception e) {
                helper.abort(transactionInvocationContext.getIdentifier(), instanceId);
                transactionInvocationContext.abort();
            }
        }

        TransactionInvocationContext c = transactionInvocationContext;
        transactionInvocationContext = null; // 提交后该manager对象可以进行重用
        return c;
    }

    @Override
    public TransactionInvocationContext commit(long timeout, TimeUnit unit)
            throws TransactionException, TimeoutException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }

        transactionInvocationContext.getFuture().combineDone();
        if (transactionInvocationContext.getFuture().isDone()) {
            if (transactionInvocationContext.getFuture().isError()) {
                helper.abort(transactionInvocationContext.getIdentifier(), instanceId);
                transactionInvocationContext.abort();
            } else {
                helper.commit(transactionInvocationContext.getIdentifier(), instanceId);
                transactionInvocationContext.commit();
            }
        } else {
            try {
                transactionInvocationContext.getFuture().get(timeout, unit);
                return commit(timeout, unit);
            } catch (InterruptedException | ExecutionException e) {
                helper.abort(transactionInvocationContext.getIdentifier(), instanceId);
                transactionInvocationContext.abort();
            }
        }

        TransactionInvocationContext c = transactionInvocationContext;
        transactionInvocationContext = null; // 提交后该manager对象可以进行重用
        return c;
    }

    @Override
    public TransactionInvocationContext abort() throws TransactionException {
        if (!isTransaction()) {
            throw new TransactionException("Transaction does not begin with this proxy");
        }
        LOGGER.debug("Transaction id={} will abort.", transactionInvocationContext.getIdentifier());
        transactionInvocationContext.getFuture().combineDone();
        helper.abort(transactionInvocationContext.getIdentifier(), instanceId);
        transactionInvocationContext.abort();
        TransactionInvocationContext c = transactionInvocationContext;
        transactionInvocationContext = null; // 提交后该manager对象可以进行重用
        return c;
    }

    @Override
    public boolean isAvailable() {
        return transactionInvocationContext == null;
    }

    @Override
    public void destroy() {
        ObjectLifecycleUtils.destroy(helper);
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> T asyncService(ServiceInterface<T> serviceInterface) {
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
                    TransactionRequestPayload request = new TransactionRequestPayload(
                            transactionInvocationContext.getIdentifier(),
                            nextSerialId(), serviceName, methodName, args);
                    InvocationContext invocationContext = new InvocationContext();
                    invocationContext.setRequest(request);
                    invocationContext.setChoosedInstanceCondition(instanceCondition());
                    invocationContext.setTimeoutThreshold(timeoutThreshold());

                    Consumer<ServiceInstance> proxyBindingChoosedInstancePostProcessor = choosedInstancePostProcessor();
                    invocationContext.setChoosedInstancePostProcessor(instance -> {
                        try {
                            helper.prepare(request.transactionId(), request.serialId(), instance.id());
                        } catch (TransactionException e) {
                            LOGGER.error("Transaction message(transactionId={}, serialId={}) prepare failed. {}",
                                    request.transactionId(), request.serialId(), e.getMessage());
                            invocationContext.getFuture().done(true, e);
                        }
                        if (proxyBindingChoosedInstancePostProcessor != null) {
                            proxyBindingChoosedInstancePostProcessor.accept(instance);
                        }
                    });
                    call(invocationContext);
                    AbstractTransactionManager.this.transactionInvocationContext.getFuture()
                            .combine(invocationContext.getFuture());
                    AbstractTransactionManager.this.transactionInvocationContext.getInvocationContextList()
                            .add(invocationContext);
                    return invocationContext;
                } catch (Exception e) {
                    return super.call(serviceName, methodName, args);
                }
            } else {
                return super.call(serviceName, methodName, args);
            }
        }
    }
}

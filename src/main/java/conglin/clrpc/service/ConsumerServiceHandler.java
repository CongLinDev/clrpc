package conglin.clrpc.service;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.registry.ServiceDiscovery;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.fallback.DefaultFallbackHolder;
import conglin.clrpc.service.fallback.FallbackHolder;
import conglin.clrpc.service.future.DefaultFuturesHolder;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.BasicProxy;
import conglin.clrpc.service.proxy.SyncObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.service.proxy.ZooKeeperTransactionProxy;
import conglin.clrpc.zookeeper.registry.ZooKeeperServiceDiscovery;

public class ConsumerServiceHandler extends AbstractServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerServiceHandler.class);

    private final FuturesHolder<Long> futuresHolder;

    private final FallbackHolder fallbackHolder;

    private final ServiceDiscovery serviceDiscovery;

    private ConsumerContext context;

    public ConsumerServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        futuresHolder = new DefaultFuturesHolder();
        fallbackHolder = new DefaultFallbackHolder(configurer);
        serviceDiscovery = new ZooKeeperServiceDiscovery(configurer);
    }

    /**
     * 获取同步服务代理
     * 
     * @param <T>
     * @param interfaceClass
     */
    @SuppressWarnings("unchecked")
    public <T> T getSyncProxy(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { interfaceClass },
                new SyncObjectProxy(interfaceClass, context.getRequestSender(), context.getIdentifierGenerator()));
    }

    /**
     * 获取异步服务代理
     * 
     * @param <T>
     * @param interfaceClass
     */
    @SuppressWarnings("unchecked")
    public <T> T getAsyncProxy(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { interfaceClass },
                new AsyncObjectProxy(interfaceClass, context.getRequestSender(), context.getIdentifierGenerator()));
    }

    /**
     * 获取基本的异步服务代理
     * 
     * @return
     */
    public BasicProxy getBasicProxy() {
        return new BasicProxy(context.getRequestSender(), context.getIdentifierGenerator());
    }

    /**
     * 获取事务服务代理
     * 
     * @return
     */
    public TransactionProxy getTransactionProxy() {
        return new ZooKeeperTransactionProxy(context.getRequestSender(), context.getIdentifierGenerator(),
                context.getPropertyConfigurer());
    }

    /**
     * 准备工作
     * 
     * @param serviceName
     * @param interfaceClass
     */
    public void prepare(String serviceName, Class<?> interfaceClass) {
        fallbackHolder.add(serviceName, interfaceClass);
    }

    /**
     * 启动 获得请求发送器，用于检查超时Future 重发请求
     * 
     * @param context
     */
    public void start(ConsumerContext context) {
        this.context = context;
        initContext(context);
    }

    /**
     * 初始化上下文
     * 
     * @param context
     */
    protected void initContext(ConsumerContext context) {
        super.initContext(context);
        context.setServiceRegister(serviceDiscovery);
        context.setFuturesHolder(futuresHolder);
        context.setFallbackHolder(fallbackHolder);
    }

    /**
     * 停止
     */
    public void stop() {
        futuresHolder.waitForUncompleteFuture();

        if (!super.isDestroyed()) {
            try {
                super.destroy();
            } catch (DestroyFailedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * 发现服务
     * 
     * @param serviceName
     * @param updateMethod
     */
    public void findService(String serviceName, BiConsumer<String, Collection<Pair<String, String>>> updateMethod) {
        serviceDiscovery.discover(serviceName, updateMethod);
    }
}
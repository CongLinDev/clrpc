package conglin.clrpc.service;

import java.lang.reflect.Proxy;

import conglin.clrpc.common.registry.DiscoveryCallback;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.registry.ServiceDiscovery;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.future.DefaultFutureHolder;
import conglin.clrpc.service.future.FutureHolder;

public class ConsumerServiceHandler extends AbstractServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerServiceHandler.class);

    private final FutureHolder<Long> futureHolder;

    private final ServiceDiscovery serviceDiscovery;

    public ConsumerServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        futureHolder = new DefaultFutureHolder();
        String discoveryClassName = configurer.get("registry.discovery-class", String.class);
        String registryUrl = configurer.get("registry.url", String.class);
        serviceDiscovery = ClassUtils.loadObjectByType(discoveryClassName, ServiceDiscovery.class, new UrlScheme(registryUrl));
    }

    /**
     * 获取同步服务代理
     * 
     * @param <T>
     * @param serviceInterface
     */
    @SuppressWarnings("unchecked")
    public <T> T getSyncProxy(ServiceInterface<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() },
                new SyncObjectProxy(serviceInterface, context().getWith(RpcContextEnum.REQUEST_SENDER), context().getWith(RpcContextEnum.IDENTIFIER_GENERATOR)));
    }

    /**
     * 获取异步服务代理
     * 
     * @param <T>
     * @param serviceInterface
     */
    @SuppressWarnings("unchecked")
    public <T> T getAsyncProxy(ServiceInterface<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() },
                new AsyncObjectProxy(serviceInterface, context().getWith(RpcContextEnum.REQUEST_SENDER), context().getWith(RpcContextEnum.IDENTIFIER_GENERATOR)));
    }

    /**
     * 获取基本的异步服务代理
     * 
     * @return
     */
    public AnonymousProxy getAnonymousProxy() {
        return new AnonymousProxy(new BasicProxy(context().getWith(RpcContextEnum.REQUEST_SENDER), context().getWith(RpcContextEnum.IDENTIFIER_GENERATOR)));
    }

    /**
     * 获取事务服务代理
     * 
     * @return
     */
    public TransactionProxy getTransactionProxy() {
        PropertyConfigurer c = context().getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        String transactionProxyClassName = c.get("atomicity.transaction.proxy-class", String.class);
        return ClassUtils.loadObjectByType(transactionProxyClassName, TransactionProxy.class, context());
    }

    /**
     * 启动 获得请求发送器，用于检查超时Future 重发请求
     * 
     * @param context
     */
    public void start(RpcContext context) {
        super.start(context);
        context.put(RpcContextEnum.SERVICE_REGISTRY, serviceDiscovery);
        context.put(RpcContextEnum.FUTURE_HOLDER, futureHolder);
    }

    /**
     * 停止
     */
    public void stop() {
        futureHolder.waitForUncompletedFuture();

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
    public void findService(String serviceName, DiscoveryCallback updateMethod) {
        serviceDiscovery.discover(serviceName, updateMethod);
    }
}
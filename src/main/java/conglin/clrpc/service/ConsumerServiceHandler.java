package conglin.clrpc.service;

import java.lang.reflect.Proxy;

import conglin.clrpc.common.registry.DiscoveryCallback;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.common.util.ObjectUtils;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.proxy.*;
import conglin.clrpc.service.util.ObjectAssemblyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.config.PropertyConfigurer;
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
        SyncObjectProxy proxy = new SyncObjectProxy(serviceInterface);
        ObjectAssemblyUtils.assemble(proxy, context());
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() },
                proxy);
    }

    /**
     * 获取异步服务代理
     * 
     * @param <T>
     * @param serviceInterface
     */
    @SuppressWarnings("unchecked")
    public <T> T getAsyncProxy(ServiceInterface<T> serviceInterface) {
        AsyncObjectProxy proxy = new AsyncObjectProxy(serviceInterface);
        ObjectAssemblyUtils.assemble(proxy, context());
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() }, proxy);
    }

    /**
     * 获取基本的异步服务代理
     * 
     * @return
     */
    public AnonymousProxy getAnonymousProxy() {
        BasicProxy proxy = new BasicProxy();
        ObjectAssemblyUtils.assemble(proxy, context());
        return new AnonymousProxy(proxy);
    }

    /**
     * 获取代理
     *
     * @param clazz
     * @return
     */
    public RpcProxy getProxy(Class<? extends RpcProxy> clazz) {
        RpcProxy proxy = ClassUtils.loadObjectByType(clazz, RpcProxy.class);
        ObjectAssemblyUtils.assemble(proxy, context());
        return proxy;
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
        ObjectUtils.destroy(this);
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
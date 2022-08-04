package conglin.clrpc.bootstrap;

import java.lang.reflect.Proxy;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.BootOption;
import conglin.clrpc.common.CommonState;
import conglin.clrpc.common.Role;
import conglin.clrpc.common.StateRecord;
import conglin.clrpc.executor.InvocationContextExecutor;
import conglin.clrpc.executor.NetworkClientExecutor;
import conglin.clrpc.executor.pipeline.ChainExecutor;
import conglin.clrpc.executor.pipeline.CommonExecutorPipeline;
import conglin.clrpc.invocation.proxy.AsyncObjectProxy;
import conglin.clrpc.invocation.proxy.ServiceInterfaceObjectProxy;
import conglin.clrpc.invocation.proxy.SyncObjectProxy;
import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.ObjectLifecycleUtils;
import conglin.clrpc.netty.NettyRouter;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.loadbalance.ConsistentHashLoadBalancer;
import conglin.clrpc.service.loadbalance.LoadBalancer;
import conglin.clrpc.service.registry.ServiceRegistryFactory;
import conglin.clrpc.service.router.Router;

/**
 * RPC consumer端启动类
 * 
 * 使用如下代码启动
 * 
 * <blockquote>
 *
 * <pre>
 * ServiceInterface<Interface1> serviceInterface1 = ...
 * 
 * ConsumerBootstrap bootstrap = new ConsumerBootstrap();
 * bootstrap.registry(new ServiceRegistryFactory(){...}).start(new BootOption());
 *
 * bootstrap.subscribe(serviceInterface1);
 *
 * Interface1 sync = bootstrap.syncService(serviceInterface1);
 *
 * Interface1 async = bootstrap.asyncService(serviceInterface1);
 *
 * bootstrap.stop();
 * </pre>
 *
 * </blockquote>
 * 注意：结束后不要忘记关闭客户端，释放资源。
 */
public class ConsumerBootstrap extends Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBootstrap.class);

    private final StateRecord<CommonState> stateRecord;
    private final CommonExecutorPipeline executorPipeline;
    private final Router router;

    private ComponentContext context;

    public ConsumerBootstrap() {
        this(null);
    }

    public ConsumerBootstrap(Properties properties) {
        super(properties);
        this.router = new NettyRouter();
        stateRecord = new StateRecord<>(CommonState.PREPARE);
        executorPipeline = new CommonExecutorPipeline();
    }

    /**
     * 注册处理器
     * 
     * @param executor
     * @return
     */
    public ConsumerBootstrap registerExecutor(ChainExecutor executor) {
        stateRecord.except(CommonState.PREPARE);
        executorPipeline.register(executor);
        return this;
    }

    /**
     * 取消注册处理器
     * 
     * @param name
     * @return
     */
    public ConsumerBootstrap unregisterExecutor(String name) {
        stateRecord.except(CommonState.PREPARE);
        executorPipeline.unregister(name);
        return this;
    }

    /**
     * 设置注册中心
     * 
     * @param registryFactory
     * @return
     */
    public ConsumerBootstrap registry(ServiceRegistryFactory registryFactory) {
        stateRecord.except(CommonState.PREPARE);
        this.router.bindRegistryFactory(registryFactory);
        return this;
    }

    @Override
    public Role role() {
        return Role.CONSUMER;
    }

    /**
     * 获取异步服务代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link ConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     *
     * @param <T>
     * @param serviceInterface 接口
     * @return 代理服务对象
     */
    @SuppressWarnings("unchecked")
    public <T> T asyncService(ServiceInterface<T> serviceInterface) {
        stateRecord.except(CommonState.AVAILABLE);
        ServiceInterfaceObjectProxy proxy = new AsyncObjectProxy(serviceInterface);
        ObjectLifecycleUtils.assemble(proxy, context);
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() }, proxy);
    }

    /**
     * 获取同步服务代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link ConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     *
     * @param <T>
     * @param serviceInterface 接口
     * @return 代理服务对象
     */
    @SuppressWarnings("unchecked")
    public <T> T syncService(ServiceInterface<T> serviceInterface) {
        stateRecord.except(CommonState.AVAILABLE);
        ServiceInterfaceObjectProxy proxy = new SyncObjectProxy(serviceInterface);
        ObjectLifecycleUtils.assemble(proxy, context);
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { serviceInterface.interfaceClass() }, proxy);
    }

    /**
     * 刷新服务
     *
     * @param serviceInterface 接口
     * @return this
     */
    public ConsumerBootstrap subscribe(ServiceInterface<?> serviceInterface) {
        return subscribe(serviceInterface, ConsistentHashLoadBalancer.class);
    }

    /**
     * 刷新服务
     *
     * @param serviceInterface    接口
     * @param loaderBalancerClass 指定 {@link LoadBalancer}
     * @return this
     */
    public ConsumerBootstrap subscribe(ServiceInterface<?> serviceInterface, Class<?> loaderBalancerClass) {
        stateRecord.except(CommonState.AVAILABLE);
        LOGGER.debug("Refresh service=({}) provider.", serviceInterface.name());
        router.subscribe(serviceInterface, loaderBalancerClass);
        return this;
    }

    @Override
    public Object object(Class<?> clazz) {
        stateRecord.except(CommonState.AVAILABLE);
        return super.object(clazz);
    }

    /**
     * 启动
     *
     * @param option 启动选项
     */
    public void start(BootOption option) {
        if (stateRecord.compareAndSetState(CommonState.PREPARE, CommonState.INITING)) {
            LOGGER.info("ConsumerBootstrap is starting.");
            initContext(option);
            ObjectLifecycleUtils.assemble(router, context);
            executorPipeline.register(new InvocationContextExecutor());
            executorPipeline.register(new NetworkClientExecutor());
            ObjectLifecycleUtils.assemble(executorPipeline, context);
            stateRecord.setState(CommonState.AVAILABLE);
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (stateRecord.compareAndSetState(CommonState.AVAILABLE, CommonState.DESTORYING)) {
            LOGGER.info("Consumer is stopping.");
            ObjectLifecycleUtils.destroy(router);
            ObjectLifecycleUtils.destroy(executorPipeline);
            context = null;
            stateRecord.setState(CommonState.UNAVAILABLE);
        }
    }

    /**
     * 关闭钩子
     *
     * @return this
     */
    public ConsumerBootstrap hookStop() {
        hook(this::stop);
        return this;
    }

    /**
     * 初始化上下文
     *
     * @param option
     * @return context
     */
    private void initContext(BootOption option) {
        context = new ComponentContext();
        // 设置角色
        context.put(ComponentContextEnum.ROLE, role());
        // 设置属性配置器
        context.put(ComponentContextEnum.PROPERTIES, properties());
        // 设置序列化处理器
        context.put(ComponentContextEnum.SERIALIZATION_HANDLER, option.serializationHandler());
        // 设置ID生成器
        context.put(ComponentContextEnum.IDENTIFIER_GENERATOR, option.identifierGenerator());
        // codec
        context.put(ComponentContextEnum.SERVICE_INSTANCE_CODEC, option.serviceInstanceCodec());
        // protocol
        context.put(ComponentContextEnum.PROTOCOL_DEFINITION, option.protocolDefinition());
        // router
        context.put(ComponentContextEnum.ROUTER, router);
        // executor pipeline
        context.put(ComponentContextEnum.EXECUTOR_PIPELINE, executorPipeline);
    }

    @Override
    protected ComponentContext componentContext() {
        return context;
    }

}

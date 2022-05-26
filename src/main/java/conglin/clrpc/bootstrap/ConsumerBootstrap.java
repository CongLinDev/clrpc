package conglin.clrpc.bootstrap;

import java.lang.reflect.Proxy;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.BootOption;
import conglin.clrpc.common.CommonState;
import conglin.clrpc.common.Role;
import conglin.clrpc.common.StateRecord;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.context.DefaultInvocationContextHolder;
import conglin.clrpc.service.context.InvocationContextHolder;
import conglin.clrpc.service.proxy.AsyncObjectProxy;
import conglin.clrpc.service.proxy.ServiceInterfaceObjectProxy;
import conglin.clrpc.service.proxy.SyncObjectProxy;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.component.DefaultInvocationExecutor;
import conglin.clrpc.transport.component.InvocationExecutor;
import conglin.clrpc.transport.router.NettyRouter;
import conglin.clrpc.transport.router.Router;

/**
 * RPC consumer端启动类
 * <p>
 * 使用如下代码启动
 *
 * <blockquote>
 *
 * <pre>
 * ConsumerBootstrap bootstrap = new ConsumerBootstrap();
 * bootstrap.registry(new ServiceRegistry(){...}).start(new BootOption());
 *
 * // 构造ServiceInterface
 * ServiceInterface<Interface1> serviceInterface1 = new SimpleServiceInterface.Builder<>(Interface1.class)
 *         .name("Service1")
 *         .build();
 * // 刷新
 * bootstrap.subscribe(serviceInterface1);
 *
 * // 订阅同步服务
 * Interface1 sync = bootstrap.proxy(serviceInterface1, false);
 *
 * // 订阅异步服务
 * Interface1 async = bootstrap.proxy(serviceInterface1, true);
 *
 * bootstrap.stop();
 * </pre>
 *
 * </blockquote>
 * <p>
 * 注意：结束后不要忘记关闭客户端，释放资源。
 */
public class ConsumerBootstrap extends Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBootstrap.class);

    private final StateRecord<CommonState> stateRecord;
    private final InvocationContextHolder<Long> contextHolder;
    private final Router router;
    private final InvocationExecutor invocationExecutor;

    private ServiceRegistry serviceRegistry;
    private ComponentContext context;

    public ConsumerBootstrap() {
        this(null);
    }

    public ConsumerBootstrap(Properties properties) {
        super(properties);
        this.contextHolder = new DefaultInvocationContextHolder();
        this.router = new NettyRouter();
        invocationExecutor = new DefaultInvocationExecutor();
        stateRecord = new StateRecord<>(CommonState.PREPARE);
    }

    /**
     * 设置注册中心
     * 
     * @param serviceRegistry
     * @return
     */
    public ConsumerBootstrap registry(ServiceRegistry serviceRegistry) {
        stateRecord.except(CommonState.PREPARE);
        this.serviceRegistry = serviceRegistry;
        return this;
    }

    @Override
    public Role role() {
        return Role.CONSUMER;
    }

    /**
     * 获取异步服务代理
     * <p>
     * 使用该方法返回的代理前，应当保证之前调用 {@link ConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     *
     * @param <T>
     * @param serviceInterface 接口
     * @return 代理服务对象
     * @see #proxy(ServiceInterface, boolean)
     */
    public <T> T proxy(ServiceInterface<T> serviceInterface) {
        return proxy(serviceInterface, true);
    }

    /**
     * 获取服务代理
     * <p>
     * 使用该方法返回的代理前，应当保证之前调用 {@link ConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     *
     * @param <T>
     * @param serviceInterface 接口
     * @param async            是否是异步代理
     * @return 代理服务对象
     */
    @SuppressWarnings("unchecked")
    public <T> T proxy(ServiceInterface<T> serviceInterface, boolean async) {
        stateRecord.except(CommonState.AVAILABLE);
        ServiceInterfaceObjectProxy proxy = async ? new AsyncObjectProxy(serviceInterface)
                : new SyncObjectProxy(serviceInterface);
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
        stateRecord.except(CommonState.AVAILABLE);
        LOGGER.debug("Refresh service=({}) provider.", serviceInterface.name());
        router.subscribe(serviceInterface);
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
            ObjectLifecycleUtils.assemble(contextHolder, context);
            ObjectLifecycleUtils.assemble(router, context);
            ObjectLifecycleUtils.assemble(invocationExecutor, context);
            stateRecord.setState(CommonState.AVAILABLE);
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (stateRecord.compareAndSetState(CommonState.AVAILABLE, CommonState.DESTORYING)) {
            LOGGER.info("Consumer is stopping.");
            contextHolder.waitForUncompletedInvocation();
            ObjectLifecycleUtils.destroy(contextHolder);
            ObjectLifecycleUtils.destroy(router);
            ObjectLifecycleUtils.destroy(invocationExecutor);
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
        assert serviceRegistry != null;
        // registry
        context.put(ComponentContextEnum.SERVICE_REGISTRY, serviceRegistry);
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
        // future holder
        context.put(ComponentContextEnum.INVOCATION_CONTEXT_HOLDER, contextHolder);
        // router
        context.put(ComponentContextEnum.ROUTER, router);
        // request sender
        context.put(ComponentContextEnum.INVOCATION_EXECUTOR, invocationExecutor);
        // channelHandlerFactory
        context.put(ComponentContextEnum.CHANNEL_HANDLER_FACTORY, option.channelHandlerFactory());
    }

    @Override
    protected ComponentContext componentContext() {
        return context;
    }

}

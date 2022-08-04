package conglin.clrpc.bootstrap;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.BootOption;
import conglin.clrpc.common.Role;
import conglin.clrpc.common.CommonState;
import conglin.clrpc.common.StateRecord;
import conglin.clrpc.executor.CommonRequestExecutor;
import conglin.clrpc.executor.pipeline.ChainExecutor;
import conglin.clrpc.executor.pipeline.CommonExecutorPipeline;
import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.ObjectLifecycleUtils;
import conglin.clrpc.netty.NettyPublisher;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.ServiceObjectHolder;
import conglin.clrpc.service.publisher.Publisher;
import conglin.clrpc.service.registry.ServiceRegistryFactory;

/**
 * RPC provider端启动类
 * 
 * 使用如下代码启动
 *
 * <blockquote>
 *
 * <pre>
 *
 * ServiceObject<Interface1> serviceObject = ...
 * 
 * ProviderBootstrap bootstrap = new ProviderBootstrap();
 *
 * bootstrap.registry(new ServiceRegistryFactory(){...})
 *          .publish(serviceObject).hookStop().start(new BootOption());
 *
 * </pre>
 *
 * </blockquote>
 * 
 * 注意：若服务接口相同，先添加的服务会被覆盖。 结束后不要忘记关闭服务端，释放资源。
 */
public class ProviderBootstrap extends Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBootstrap.class);

    private final StateRecord<CommonState> stateRecord;
    private final Publisher publisher;
    private final ServiceObjectHolder serviceObjectHolder;
    private ComponentContext context;
    private final CommonExecutorPipeline executorPipeline;

    public ProviderBootstrap() {
        this(null);
    }

    /**
     * 注册处理器
     * 
     * @param executor
     * @return
     */
    public ProviderBootstrap registerExecutor(ChainExecutor executor) {
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
    public ProviderBootstrap unregisterExecutor(String name) {
        stateRecord.except(CommonState.PREPARE);
        executorPipeline.unregister(name);
        return this;
    }

    /**
     * 创建 服务提供者 启动对象
     *
     * @param properties 配置
     */
    public ProviderBootstrap(Properties properties) {
        super(properties);
        serviceObjectHolder = new ServiceObjectHolder();
        publisher = new NettyPublisher();
        stateRecord = new StateRecord<>(CommonState.PREPARE);
        executorPipeline = new CommonExecutorPipeline();
    }

    /**
     * 发布单例服务
     *
     * @param serviceObject 服务对象
     * @return
     */
    public ProviderBootstrap publish(ServiceObject<?> serviceObject) {
        stateRecord.except(CommonState.PREPARE);
        serviceObjectHolder.putServiceObject(serviceObject);
        LOGGER.info("Publish service named {} with interface(class={}).", serviceObject.name(),
                serviceObject.interfaceClass());
        return this;
    }

    @Override
    final public Role role() {
        return Role.PROVIDER;
    }

    /**
     * 设置注册中心
     * 
     * @param registryFactory
     * @return
     */
    public ProviderBootstrap registry(ServiceRegistryFactory registryFactory) {
        stateRecord.except(CommonState.PREPARE);
        this.publisher.bindRegistryFactory(registryFactory);
        return this;
    }

    /**
     * 启动。该方法会一直阻塞，直到被显示关闭 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     *
     * @param option 启动选项
     */
    public void start(BootOption option) {
        if (stateRecord.compareAndSetState(CommonState.PREPARE, CommonState.INITING)) {
            LOGGER.info("Provider is starting.");
            executorPipeline.register(new CommonRequestExecutor());
            ComponentContext context = initContext(option);
            ObjectLifecycleUtils.assemble(serviceObjectHolder, context);
            ObjectLifecycleUtils.assemble(executorPipeline, context);
            ObjectLifecycleUtils.assemble(publisher, context);
            stateRecord.setState(CommonState.AVAILABLE);
        }
    }

    /**
     * 关闭
     */
    public void stop() {
        if (stateRecord.compareAndSetState(CommonState.AVAILABLE, CommonState.DESTORYING)) {
            LOGGER.info("Provider is stopping.");
            ObjectLifecycleUtils.destroy(serviceObjectHolder);
            ObjectLifecycleUtils.destroy(publisher);
            context = null;
            stateRecord.setState(CommonState.UNAVAILABLE);
        }
    }

    /**
     * 关闭钩子
     *
     * @return this
     */
    public ProviderBootstrap hookStop() {
        stateRecord.except(CommonState.PREPARE);
        hook(this::stop);
        return this;
    }

    /**
     * 初始化上下文
     *
     * @param option
     * @return
     */
    private ComponentContext initContext(BootOption option) {
        context = new ComponentContext();
        context.put(ComponentContextEnum.SERVICE_OBJECT_HOLDER, this.serviceObjectHolder);
        // 设置角色
        context.put(ComponentContextEnum.ROLE, role());
        // 设置属性配置器
        context.put(ComponentContextEnum.PROPERTIES, properties());
        // 设置序列化处理器
        context.put(ComponentContextEnum.SERIALIZATION_HANDLER, option.serializationHandler());
        // codec
        context.put(ComponentContextEnum.SERVICE_INSTANCE_CODEC, option.serviceInstanceCodec());
        // protocol
        context.put(ComponentContextEnum.PROTOCOL_DEFINITION, option.protocolDefinition());
        // executor pipeline
        context.put(ComponentContextEnum.EXECUTOR_PIPELINE, executorPipeline);
        return context;
    }

    @Override
    protected ComponentContext componentContext() {
        return context;
    }
}

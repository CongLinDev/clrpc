package conglin.clrpc.bootstrap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.BootOption;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.definition.role.Role;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.publisher.NettyPublisher;
import conglin.clrpc.transport.publisher.Publisher;

/**
 * RPC provider端启动类
 * <p>
 * 使用如下代码启动
 *
 * <blockquote>
 *
 * <pre>
 *
 * ProviderBootstrap bootstrap = new ProviderBootstrap();
 * ServiceObject<Interface1> serviceObject = new SimpleServiceObject.Builder<>(Interface1.class)
 *         .name("Service1")
 *         .object(new ServiceImpl1())
 *         .build();
 * bootstrap.publish(serviceObject).hookStop().start(new BootOption());
 *
 * </pre>
 *
 * </blockquote>
 * <p>
 * 注意：若服务接口相同，先添加的服务会被覆盖。 结束后不要忘记关闭服务端，释放资源。
 */
public class ProviderBootstrap extends Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBootstrap.class);

    private final Publisher publisher;
    private final Map<String, ServiceObject<?>> serviceObjects;

    private ComponentContext context;

    public ProviderBootstrap() {
        this(null);
    }

    /**
     * 创建 服务提供者 启动对象
     *
     * @param properties 配置
     */
    public ProviderBootstrap(Properties properties) {
        super(properties);
        serviceObjects = new HashMap<>();
        String registryClassName = properties().getProperty("registry.register-class");
        String registryUrl = properties().getProperty("registry.url");
        ServiceRegistry serviceRegistry = ClassUtils.loadObjectByType(registryClassName, ServiceRegistry.class,
                new UrlScheme(registryUrl));
        publisher = new NettyPublisher(serviceRegistry);
    }

    /**
     * 发布单例服务
     *
     * @param serviceObject 服务对象
     * @return
     */
    public ProviderBootstrap publish(ServiceObject<?> serviceObject) {
        serviceObjects.put(serviceObject.name(), serviceObject);
        LOGGER.info("Publish service named {} with interface(class={}).", serviceObject.name(),
                serviceObject.interfaceClass());
        return this;
    }

    @Override
    final public Role role() {
        return Role.PROVIDER;
    }

    /**
     * 启动。该方法会一直阻塞，直到被显示关闭 若调用该方法后还有其他逻辑，建议使用多线程进行编程
     *
     * @param option 启动选项
     */
    public void start(BootOption option) {
        LOGGER.info("Provider is starting.");
        ComponentContext context = initContext(option);
        ObjectLifecycleUtils.assemble(serviceObjects);
        ObjectLifecycleUtils.assemble(publisher, context);
    }

    /**
     * 关闭
     */
    public void stop() {
        LOGGER.info("Provider is stopping.");
        ObjectLifecycleUtils.destroy(serviceObjects);
        ObjectLifecycleUtils.destroy(publisher);
        context = null;
    }

    /**
     * 关闭钩子
     *
     * @return this
     */
    public ProviderBootstrap hookStop() {
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
        context.put(ComponentContextEnum.SERVICE_OBJECT_HOLDER, this.serviceObjects);
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
        return context;
    }

    @Override
    protected ComponentContext componentContext() {
        return context;
    }
}

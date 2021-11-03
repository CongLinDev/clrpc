package conglin.clrpc.bootstrap;

import conglin.clrpc.bootstrap.option.RpcOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.proxy.RpcProxy;
import conglin.clrpc.transport.ConsumerTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC consumer端启动类
 * 
 * 使用如下代码启动
 * 
 * <blockquote>
 * 
 * <pre>
 * RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
 * bootstrap.start(new CommonOption());
 *
 * // 构造ServiceInterface
 * ServiceInterface<Interface1> serviceInterface1 = SimpleServiceInterfaceBuilder.builder()
 *                              .name("Service1")
 *                              .interfaceClass(Interface1.class)
 *                              .build();
 * // 刷新
 * bootstrap.subscribe(serviceInterface1);
 * 
 * // 订阅同步服务
 * Interface1 sync = bootstrap.proxy(serviceInterface1);
 * 
 * // 订阅异步服务
 * Interface1 async = bootstrap.proxy(serviceInterface1, true);
 *
 * bootstrap.stop();
 * </pre>
 * 
 * </blockquote>
 * 
 * 注意：结束后不要忘记关闭客户端，释放资源。
 */

public class RpcConsumerBootstrap extends RpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerBootstrap.class);

    private final ConsumerTransfer CONSUMER_TRANSFER;
    private final ConsumerServiceHandler SERVICE_HANDLER;

    /**
     * 创建 服务消费者 启动对象
     * 
     * @param configurer 配置器
     */
    public RpcConsumerBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        SERVICE_HANDLER = new ConsumerServiceHandler(configurer());
        CONSUMER_TRANSFER = new ConsumerTransfer();
    }

    /**
     * 获取异步服务代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     * 
     * @see #proxy(ServiceInterface, boolean)
     * 
     * @param <T>
     * @param serviceInterface 接口
     * @return 代理服务对象
     */
    public <T> T proxy(ServiceInterface<T> serviceInterface) {
        return proxy(serviceInterface, true);
    }

    /**
     * 获取服务代理
     *
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     *
     * @param <T>
     * @param serviceInterface 接口
     * @param async            是否是异步代理
     * @return 代理服务对象
     */
    public <T> T proxy(ServiceInterface<T> serviceInterface, boolean async) {
        return async ? SERVICE_HANDLER.getAsyncProxy(serviceInterface) : SERVICE_HANDLER.getSyncProxy(serviceInterface);
    }

    /**
     * 刷新服务
     *
     * @param serviceInterface 接口
     * @return this
     */
    public RpcConsumerBootstrap subscribe(ServiceInterface<?> serviceInterface) {
        String serviceName = serviceInterface.name();
        if (CONSUMER_TRANSFER.needRefresh(serviceName)) {
            LOGGER.debug("Refresh service=({}) provider.", serviceName);
            SERVICE_HANDLER.findService(serviceName, CONSUMER_TRANSFER::updateConnectedProvider);
        }
        return this;
    }

    /**
     * 返回一个自定义代理，该代理必须提供一个无参函数
     *
     * @param clazz
     * @return
     */
    public RpcProxy proxy(Class<? extends RpcProxy> clazz) {
        return SERVICE_HANDLER.getProxy(clazz);
    }


    @Override
    final public Role role() {
        return Role.CONSUMER;
    }

    /**
     * 启动
     * 
     * @param option 启动选项
     */
    public void start(RpcOption option) {
        LOGGER.info("RpcConsumer is starting.");
        RpcContext context = initContext(option);
        SERVICE_HANDLER.start(context);
        CONSUMER_TRANSFER.start(context);
    }

    /**
     * 停止
     */
    public void stop() {
        LOGGER.info("RpcConsumer is stopping.");
        SERVICE_HANDLER.stop();
        CONSUMER_TRANSFER.stop();
    }

    /**
     * 关闭钩子
     * 
     * @return this
     */
    public RpcConsumerBootstrap hookStop() {
        hook(this::stop);
        return this;
    }

    /**
     * 初始化上下文
     * 
     * @param option
     * @return context
     */
    private RpcContext initContext(RpcOption option) {
        RpcContext context = new RpcContext();
        // 设置角色
        context.put(RpcContextEnum.ROLE, role());
        // 设置属性配置器
        context.put(RpcContextEnum.PROPERTY_CONFIGURER, configurer());
        // 设置序列化处理器
        context.put(RpcContextEnum.SERIALIZATION_HANDLER, option.serializationHandler());
        // 设置ID生成器
        context.put(RpcContextEnum.IDENTIFIER_GENERATOR, option.identifierGenerator());
        return context;
    }
}
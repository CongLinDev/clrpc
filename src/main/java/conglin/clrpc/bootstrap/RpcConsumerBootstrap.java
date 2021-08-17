package conglin.clrpc.bootstrap;

import conglin.clrpc.common.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcOption;
import conglin.clrpc.bootstrap.option.RpcOptionEnum;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.proxy.AnonymousProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.transport.ConsumerTransfer;

/**
 * RPC consumer端启动类
 * 
 * 使用如下代码启动
 * 
 * <blockquote>
 * 
 * <pre>
 * RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
 * bootstrap.start();
 * // 刷新
 * bootstrap.subscribe(Interface1.class).subscribe(Interface2.class);
 * 
 * // 订阅同步服务
 * Interface1 i1 = bootstrap.proxy(Interface1.class);
 * 
 * // 订阅异步服务
 * Interface2 i2 = bootstrap.proxy(Interface2.class, true);
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
     * 获取匿名代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     * 
     * @return proxy
     */
    public AnonymousProxy proxy() {
        return SERVICE_HANDLER.getAnonymousProxy();
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
     * 订阅事务服务
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(ServiceInterface)}
     * 刷新
     * 
     * @return proxy
     */
    public TransactionProxy transaction() {
        return SERVICE_HANDLER.getTransactionProxy();
    }


    @Override
    final public Role role() {
        return Role.CONSUMER;
    }

    /**
     * 启动
     */
    public void start() {
        start(new RpcOption());
    }

    /**
     * 启动
     * 
     * @param option 启动选项
     */
    public void start(RpcOption option) {
        LOGGER.info("RpcConsumer is starting.");
        super.start();
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
        super.stop();
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
        context.put(RpcContextEnum.IDENTIFIER_GENERATOR, option.getOrDefault(RpcOptionEnum.IDENTIFIER_GENERATOR));
        // 设置ID生成器
        context.put(RpcContextEnum.PROVIDER_CHOOSER_ADAPTER,
                option.getOrDefault(RpcOptionEnum.PROVIDER_CHOOSER_ADAPTER));
        // 设置服务提供者挑选适配器
        context.put(RpcContextEnum.SERIALIZATION_HANDLER, ClassUtils.loadObject(configurer().get(role().item(".message.serializationHandlerClassName"), String.class)));
        return context;
    }
}
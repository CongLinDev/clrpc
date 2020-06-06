package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcConsumerOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.annotation.AnnotationParser;
import conglin.clrpc.service.context.BasicConsumerContext;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.proxy.BasicProxy;
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
     * @see #RpcConsumerBootstrap(PropertyConfigurer)
     */
    public RpcConsumerBootstrap() {
        this(null);
    }

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
     * 获取基本代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(Class)} 刷新
     * 
     * @return proxy
     */
    public BasicProxy proxy() {
        return SERVICE_HANDLER.getBasicProxy();
    }

    /**
     * 获取异步服务代理
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(Class)} 刷新
     * 
     * @see #proxy(Class, boolean)
     * 
     * @param <T>
     * @param interfaceClass 接口类
     * @return 代理服务对象
     */
    public <T> T proxy(Class<T> interfaceClass) {
        return proxy(interfaceClass, true);
    }

    /**
     * 获取服务代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(Class)} 刷新
     * 
     * @param <T>
     * @param interfaceClass
     * @param async          是否是异步代理
     * @return 代理服务对象
     */
    public <T> T proxy(Class<T> interfaceClass, boolean async) {
        return async ? SERVICE_HANDLER.getAsyncProxy(interfaceClass) : SERVICE_HANDLER.getSyncProxy(interfaceClass);
    }

    /**
     * 刷新服务
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param interfaceClass
     * @return this
     */
    public RpcConsumerBootstrap subscribe(Class<?> interfaceClass) {
        String serviceName = AnnotationParser.serviceName(interfaceClass);
        if (serviceName == null) {
            LOGGER.error("Please Add a service name for {} by @Service.", interfaceClass);
            throw new UnsupportedOperationException();
        }

        SERVICE_HANDLER.prepare(serviceName, interfaceClass);
        if (CONSUMER_TRANSFER.needRefresh(serviceName)) {
            LOGGER.debug("Refresh service=({}) privider.", serviceName);
            SERVICE_HANDLER.findService(serviceName, CONSUMER_TRANSFER::updateConnectedProvider);
        }
        return this;
    }

    /**
     * 订阅事务服务
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#subscribe(Class)} 刷新
     * 
     * @return proxy
     */
    public TransactionProxy transaction() {
        return SERVICE_HANDLER.getTransactionProxy();
    }

    /**
     * 启动
     */
    public void start() {
        start(new RpcConsumerOption());
    }

    /**
     * 启动
     * 
     * @param option 启动选项
     */
    public void start(RpcConsumerOption option) {
        LOGGER.info("RpcConsumer is starting.");
        super.start();
        ConsumerContext context = initContext(option);

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
    private ConsumerContext initContext(RpcConsumerOption option) {
        ConsumerContext context = new BasicConsumerContext();

        // 设置属性配置器
        context.setPropertyConfigurer(configurer());

        // 设置序列化处理器
        context.setSerializationHandler(option.serializationHandler());
        // 设置ID生成器
        context.setIdentifierGenerator(option.identifierGenerator());
        // 设置服务提供者挑选适配器
        context.setProviderChooserAdapter(option.providerChooserAdapter());
        return context;
    }
}
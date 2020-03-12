package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcConsumerOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.context.BasicConsumerContext;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.proxy.CommonProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
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
 * bootstrap.refresh(Interface1.class).refresh(Interface2.class);
 * 
 * // 订阅同步服务
 * Interface1 i1 = bootstrap.subscribe(Interface1.class);
 * 
 * // 订阅异步服务
 * ObjectProxy proxy1 = bootstrap.subscribeAsync(Interface2.class);
 *
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

    public RpcConsumerBootstrap() {
        this(null);
    }

    public RpcConsumerBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        SERVICE_HANDLER = new ConsumerServiceHandler(configurer());
        CONSUMER_TRANSFER = new ConsumerTransfer();
    }

    /**
     * 获取通用代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#refresh(String)} 或
     * {@link RpcConsumerBootstrap#refresh(Class)} 刷新
     * 
     * @return proxy
     */
    public CommonProxy subscribeAsync() {
        return SERVICE_HANDLER.getPrxoy();
    }

    /**
     * 订阅同步服务，获取同步服务代理
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#refresh(Class)} 刷新
     * 
     * @param <T>
     * @param interfaceClass 接口类
     * @return 代理服务对象
     */
    public <T> T subscribe(Class<T> interfaceClass) {
        String serviceName = getServiceName(interfaceClass);
        LOGGER.info("Subscribe synchronous service named {}.", serviceName);
        return SERVICE_HANDLER.getPrxoy(interfaceClass, serviceName);
    }

    /**
     * 订阅异步服务，获取异步服务代理
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#refresh(String)} 或
     * {@link RpcConsumerBootstrap#refresh(Class)} 刷新
     * 
     * @param interfaceClass
     * @return proxy
     */
    public ObjectProxy subscribeAsync(Class<?> interfaceClass) {
        String serviceName = getServiceName(interfaceClass);
        LOGGER.info("Subscribe asynchronous service named {}.", serviceName);
        return SERVICE_HANDLER.getPrxoy(serviceName);
    }

    /**
     * 刷新服务
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param interfaceClass
     * @return this
     */
    public RpcConsumerBootstrap refresh(Class<?> interfaceClass) {
        String serviceName = getServiceName(interfaceClass);
        if (serviceName == null)
            throw new NullPointerException();
        SERVICE_HANDLER.prepare(serviceName, interfaceClass);
        if (CONSUMER_TRANSFER.needRefresh(serviceName)) {
            LOGGER.debug("Refresh Service=({}) Privider.", serviceName);
            SERVICE_HANDLER.findService(serviceName, CONSUMER_TRANSFER::updateConnectedProvider);
        }
        return this;
    }

    /**
     * 刷新并订阅同步服务，获取同步服务代理
     * 
     * 该方法相当于调用 {@link RpcConsumerBootstrap#refresh(Class)} 和
     * {@link RpcConsumerBootstrap#subscribe(Class)}
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param <T>
     * @param interfaceClass 接口类
     * @return 代理服务对象
     */
    public <T> T refreshAndSubscribe(Class<T> interfaceClass) {
        return refresh(interfaceClass).subscribe(interfaceClass);
    }

    /**
     * 订阅异步服务，获取异步服务代理
     * 
     * 该方法相当于调用 {@link RpcConsumerBootstrap#refresh(Class)} 和
     * {@link RpcConsumerBootstrap#subscribeAsync(Class)}
     * 
     * 使用 {@link conglin.clrpc.common.annotation.Service#name()} 标识服务名
     * 
     * @param interfaceClass
     * @return proxy
     */
    public ObjectProxy refreshAndSubscribeAsync(Class<?> interfaceClass) {
        return refresh(interfaceClass).subscribeAsync(interfaceClass);
    }

    /**
     * 订阅事务服务
     * 
     * 使用该方法返回的代理前，应当保证之前调用 {@link RpcConsumerBootstrap#refresh(Class)} 刷新
     * 
     * @return proxy
     */
    public TransactionProxy subscribeTransaction() {
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
        super.start();
        ConsumerContext context = initContext(option);

        SERVICE_HANDLER.start(context);
        CONSUMER_TRANSFER.start(context);
    }

    /**
     * 停止
     */
    public void stop() {
        SERVICE_HANDLER.stop();
        CONSUMER_TRANSFER.stop();
        super.stop();
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
        context.setSerializationHandler(option.getSerializationHandler());
        // 设置ID生成器
        context.setIdentifierGenerator(option.getIdentifierGenerator());
        // 设置服务提供者挑选适配器
        context.setProviderChooserAdapter(option.getProviderChooserAdapter());
        return context;
    }
}
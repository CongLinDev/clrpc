package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.option.RpcConsumerOption;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.context.BasicConsumerContext;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.transport.ConsumerTransfer;

/**
 * RPC consumer端启动类
 * 
 * 使用如下代码启动 <blockquote>
 * 
 * <pre>
 * RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
 * bootstrap.start();
 * 
 * // 订阅同步服务
 * Interface1 i1 = bootstrap.subscribe("service1");
 * Interface2 i2 = bootstrap.subscribe(Interface2.class);
 * 
 * // 订阅异步服务
 * ObjectProxy proxy = bootstrap.subscribeAsync("service3");
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
        super();
        SERVICE_HANDLER = new ConsumerServiceHandler(CONFIGURER);
        CONSUMER_TRANSFER = new ConsumerTransfer();
    }

    public RpcConsumerBootstrap(String configFilename) {
        super(configFilename);
        SERVICE_HANDLER = new ConsumerServiceHandler(CONFIGURER);
        CONSUMER_TRANSFER = new ConsumerTransfer();
    }

    public RpcConsumerBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        SERVICE_HANDLER = new ConsumerServiceHandler(CONFIGURER);
        CONSUMER_TRANSFER = new ConsumerTransfer();
    }

    /**
     * 订阅同步服务，获取同步服务代理
     * 
     * @param <T>
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public <T> T subscribe(Class<T> interfaceClass) {
        return subscribe(interfaceClass, interfaceClass.getSimpleName());
    }

    /**
     * 订阅同步服务，获取同步服务代理
     * 
     * @param <T>
     * @param interfaceClass 接口类
     * @param serviceName    服务名
     * @return 返回代理服务类
     */
    public <T> T subscribe(Class<T> interfaceClass, String serviceName) {
        LOGGER.info("Subscribe synchronous service named " + serviceName);
        SERVICE_HANDLER.findService(serviceName, CONSUMER_TRANSFER::updateConnectedProvider);
        return SERVICE_HANDLER.getPrxoy(interfaceClass, serviceName);
    }

    /**
     * 订阅异步服务，获取异步服务代理
     * 
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public ObjectProxy subscribeAsync(Class<?> interfaceClass) {
        return subscribeAsync(interfaceClass.getSimpleName());
    }

    /**
     * 订阅异步服务，获取异步服务代理
     * 
     * @param serviceName 返回代理服务类
     * @return 返回代理服务类
     */
    public ObjectProxy subscribeAsync(String serviceName) {
        LOGGER.info("Subscribe asynchronous service named " + serviceName);
        SERVICE_HANDLER.findService(serviceName, CONSUMER_TRANSFER::updateConnectedProvider);
        return SERVICE_HANDLER.getPrxoy(serviceName);
    }

    /**
     * 订阅事务服务
     * 
     * @return
     */
    public TransactionProxy subscribeAsync() {
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
     * @return
     */
    private ConsumerContext initContext(RpcConsumerOption option) {
        ConsumerContext context = new BasicConsumerContext();

        context.setLocalAddress(IPAddressUtils.getHostAddressAndPort(CONFIGURER.getOrDefault("consumer.port", 5200)));
        // 设置属性配置器
        context.setPropertyConfigurer(CONFIGURER);
        // 设置cache管理器
        context.setCacheManager(CACHE_MANAGER);

        // 设置序列化处理器
        context.setSerializationHandler(option.getSerializationHandler());
        // 设置ID生成器
        context.setIdentifierGenerator(option.getIdentifierGenerator());
        // 设置服务提供者挑选适配器
        context.setProviderChooserAdapter(option.getProviderChooserAdapter());

        return context;
    }

}
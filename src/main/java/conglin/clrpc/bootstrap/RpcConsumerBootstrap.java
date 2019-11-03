package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.codec.ProtostuffSerializationHandler;
import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.config.YamlPropertyConfigurer;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.context.BasicConsumerContext;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.transfer.ConsumerTransfer;

/**
 * RPC consumer端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
 *     bootstrap.start();
 * 
 *     // 订阅同步服务
 *     Interface1 i1 = bootstrap.subscribe("service1");
 *     Interface2 i2 = bootstrap.subscribe(Interface2.class);
 * 
 *     // 订阅异步服务
 *     ObjectProxy proxy = bootstrap.subscribeAsync("service3");
 *
 * </pre></blockquote>
 * 
 * 注意：结束后不要忘记关闭客户端，释放资源。
 */

public class RpcConsumerBootstrap extends Bootstrap {

    private static final Logger log = LoggerFactory.getLogger(RpcConsumerBootstrap.class);

    private ConsumerTransfer consumerTransfer;
    private ConsumerServiceHandler serviceHandler;

    public RpcConsumerBootstrap() {
        this(new YamlPropertyConfigurer());
    }

    public RpcConsumerBootstrap(PropertyConfigurer configurer){
        super(configurer);
        serviceHandler = new ConsumerServiceHandler(configurer);
        consumerTransfer = new ConsumerTransfer();
    }

    /**
     * 订阅同步服务，获取同步服务代理
     * @param <T>
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public <T> T subscribe(Class<T> interfaceClass){
        return subscribe(interfaceClass, interfaceClass.getSimpleName());
    }

    /**
     * 订阅同步服务，获取同步服务代理
     * @param <T>
     * @param interfaceClass 接口类
     * @param serviceName 服务名
     * @return 返回代理服务类
     */
    public <T> T subscribe(Class<T> interfaceClass, String serviceName){
        log.info("Subscribe synchronous service named " + serviceName);
        serviceHandler.findService(serviceName, consumerTransfer::updateConnectedProvider);
        return serviceHandler.getPrxoy(interfaceClass, serviceName);
    }

    /**
     * 订阅异步服务，获取异步服务代理
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public ObjectProxy subscribeAsync(Class<?> interfaceClass){
        return subscribeAsync(interfaceClass.getSimpleName());
    }

    /**
     * 订阅异步服务，获取异步服务代理
     * @param serviceName 返回代理服务类
     * @return 返回代理服务类
     */
    public ObjectProxy subscribeAsync(String serviceName){
        log.info("Subscribe asynchronous service named " + serviceName);
        serviceHandler.findService(serviceName, consumerTransfer::updateConnectedProvider);
        return serviceHandler.getPrxoy(serviceName);
    }

    /**
     * 订阅事务服务
     * @return
     */
    public TransactionProxy subscribeAsync(){
        return serviceHandler.getTransactionProxy();
    }

    /**
     * 启动
     * 序列化处理器默认使用 {@link ProtostuffSerializationHandler}
     */
    public void start() {
        start(new ProtostuffSerializationHandler());
    }

    /**
     * 启动
     * @param serializationHandler 序列化处理器
     */
    public void start(SerializationHandler serializationHandler){
        ConsumerContext context = new BasicConsumerContext();

        context.setLocalAddress(configurer.getOrDefault("consumer.address", "localhost:5200"));
        // 设置属性配置器
        context.setPropertyConfigurer(configurer);
        // 设置cache管理器
        context.setCacheManager(cacheManager);
        // 设置序列化处理器
        context.setSerializationHandler(serializationHandler);

        serviceHandler.start(context);
        consumerTransfer.start(context);
    }

    /**
     * 停止
     */
    public void stop() throws InterruptedException{
        serviceHandler.stop();
        consumerTransfer.stop();
    }

}
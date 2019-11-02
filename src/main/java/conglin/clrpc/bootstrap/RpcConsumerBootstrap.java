package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.config.YamlPropertyConfigurer;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.transfer.ConsumerTransfer;
import conglin.clrpc.transfer.receiver.BasicResponseReceiver;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import conglin.clrpc.transfer.sender.BasicRequestSender;
import conglin.clrpc.transfer.sender.RequestSender;

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

public class RpcConsumerBootstrap extends Bootstrap{

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
        return serviceHandler.getPrxoy(interfaceClass, serviceName, consumerTransfer.getSender());
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
        return serviceHandler.getPrxoy(serviceName, consumerTransfer.getSender());
    }

    /**
     * 订阅事务服务
     * @return
     */
    public TransactionProxy subscribeAsync(){
        return serviceHandler.getTransactionProxy(consumerTransfer.getSender());
    }

    /**
     * 启动
     */
    public void start(){
        ConsumerContext context = ...;

        context.setLocalAddress(configurer.getOrDefault("consumer.address", "localhost:5200"));
        // 设置属性配置器
        context.setPropertyConfigurer(configurer);
        // 设置cache管理器
        context.setCacheManager(cacheManager);

        serviceHandler.start(context);
        consumerTransfer.start(initSender(), initReceiver());
    }

    /**
     * 停止
     */
    public void stop() throws InterruptedException{
        serviceHandler.stop();
        consumerTransfer.stop();
    }

    /**
     * 获取一个 {@link RequestSender} 
     * 并调用 {@link RequestSender#init(ConsumerServiceHandler, ConsumerTransfer)} 进行初始化
     * @return
     */
    protected RequestSender initSender(){
        // String senderClassName = ConfigParser.getOrDefault("consumer.request-sender", "conglin.clrpc.transfer.sender.BasicRequestSender");
        // RequestSender sender = null;
        // try {
        //     sender = (RequestSender) Class.forName(senderClassName)
        //             .getConstructor().newInstance();
        // } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        //         | InvocationTargetException | NoSuchMethodException | SecurityException
        //         | ClassNotFoundException e) {
        //     log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.sender.BasicRequestSender' rather than "
        //             + senderClassName);
        // }finally{
        //     // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.sender.BasicRequestSender}
        //     if(sender == null) sender = new BasicRequestSender();
        // }
        RequestSender sender = new BasicRequestSender();
        sender.init(serviceHandler, consumerTransfer::chooseChannel);
        sender.bindCachePool(cacheManager);
        //serviceHandler.submit(sender);
        return sender;
    }

    protected ResponseReceiver initReceiver(){
        // String receiverClassName = ConfigParser.getOrDefault("consumer.response-receiver", "conglin.clrpc.transfer.receiver.BasicResponseReceiver");
        // ResponseReceiver receiver = null;

        // try {
        //     receiver = (ResponseReceiver) Class.forName(receiverClassName)
        //             .getConstructor().newInstance();
        // } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        //         | InvocationTargetException | NoSuchMethodException | SecurityException
        //         | ClassNotFoundException e) {
        //     log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.receiver.BasicResponseReceiver' rather than "
        //             + receiverClassName);
        // }finally{
        //     // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.receiver.BasicResponseReceiver}
        //     if(receiver == null) receiver = new BasicResponseReceiver();
        // }
        ResponseReceiver receiver = new BasicResponseReceiver();
        receiver.init(serviceHandler);
        return receiver;
    }
}
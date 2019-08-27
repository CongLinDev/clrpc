package conglin.clrpc.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.transfer.ProviderTransfer;
import conglin.clrpc.transfer.receiver.BasicRequestReceiver;
import conglin.clrpc.transfer.receiver.RequestReceiver;
import conglin.clrpc.transfer.sender.BasicResponseSender;
import conglin.clrpc.transfer.sender.ResponseSender;

/**
 * RPC provider端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcProviderBootstrap bootstrap = new RpcProviderBootstrap(); 
 *     bootstrap.publishService("service1", ServiceBean1.class)
 *              .publishService("service2", new ServiceBean2())
 *              .publishService(Interface3.class, Implement3.class)
 *              .start();
 * </pre></blockquote>
 * 
 * 注意：若服务接口相同，先添加的服务会被覆盖。
 *      结束后不要忘记关闭服务端，释放资源。
 */

public class RpcProviderBootstrap {

    private static final Logger log = LoggerFactory.getLogger(RpcProviderBootstrap.class);

    // 管理传输
    private ProviderTransfer providerTransfer;

    // 管理服务
    private ProviderServiceHandler serviceHandler;

    public final String LOCAL_ADDRESS;

    public RpcProviderBootstrap() {
        this(ConfigParser.getOrDefault("provider.address", "localhost:5100"));
    }

    public RpcProviderBootstrap(String localAddress){
        this.LOCAL_ADDRESS = localAddress;
        serviceHandler = new ProviderServiceHandler(LOCAL_ADDRESS);
        providerTransfer = new ProviderTransfer(LOCAL_ADDRESS);
    }

    /**
     * 保存即将发布的服务
     * @param interfaceClass 接口类
     * @param serviceBeanClass 实现类
     * @return
     */
    public RpcProviderBootstrap publishService(Class<?> interfaceClass, Class<?> serviceBeanClass) {
        if (!interfaceClass.isAssignableFrom(serviceBeanClass)) {
            log.error(serviceBeanClass.getSimpleName() + " is not permitted. And it will not be added Services");
            return this;
        } else {
            return publishService(interfaceClass.getSimpleName(), serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceBeanClass 类名必须满足 'xxxServiceImpl' 条件
     * @return
     */
    public RpcProviderBootstrap publishService(Class<?> serviceBeanClass) {
        String serviceBeanClassName = serviceBeanClass.getSimpleName();
        if (!serviceBeanClassName.endsWith("ServiceImpl")){
            log.error(serviceBeanClassName + " is not permitted. And you must use 'xxxServiceImpl' format classname.");
            return this;
        }else{
            return publishService(serviceBeanClassName.substring(0, serviceBeanClassName.length()-4), serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param serviceBeanClass
     * @return
     */
    public RpcProviderBootstrap publishService(String serviceName, Class<?> serviceBeanClass) {
        if (serviceBeanClass.isInterface()) {
            log.error(serviceBeanClass.getSimpleName() + " is not a service class. And it will not be added Services");
            return this;
        }else {
            serviceHandler.publishService(serviceName, serviceBeanClass);
            return this;
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param serviceBean
     * @return
     */
    public RpcProviderBootstrap publishService(String serviceName, Object serviceBean){
        serviceHandler.publishService(serviceName, serviceBean);
        return this;
    }

    /**
     * 移除已经发布的服务
     * @param interfaceClass
     * @return
     */
    public RpcProviderBootstrap removeService(Class<?> interfaceClass) {
        return removeService(interfaceClass.getSimpleName());
    }

    /**
     * 移除已经发布的服务
     * @param serviceName
     * @return
     */
    public RpcProviderBootstrap removeService(String serviceName) {
        serviceHandler.removeService(serviceName);
        return this;
    }

    /**
     * 启动
     */
    public void start() {
        // 启动Netty并将其注册到zookeeper中
        serviceHandler.start();
        providerTransfer.start(initSender(), initReceiver(), serviceHandler::registerService);

        // 关闭jvm时 调用stop方法
        // Runtime.getRuntime().addShutdownHook(new Thread(()->{
        //     this.stop();
        // }));
    }

    /**
     * 关闭
     */
    public void stop() {
        providerTransfer.stop();
        serviceHandler.stop();
    }

    /**
     * 获取一个 {@link ResponseSender} 
     * @return
     */
    protected ResponseSender initSender(){
        String senderClassName = ConfigParser.getOrDefault("consumer.response-sender", "conglin.clrpc.transfer.sender.BasicResponseSender");
        ResponseSender sender = null;
        try {
            sender = (ResponseSender) Class.forName(senderClassName)
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.sender.BasicResponseSender' rather than "
                    + senderClassName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.sender.BasicResponseSender}
            if(sender == null)
                sender = new BasicResponseSender();
        }

        return sender;
    }

    /**
     * 获取一个 {@link RequestReceiver} 
     * 并调用 {@link RequestReceiver#init(ProviderServiceHandler)} 进行初始化
     * @return
     */
    protected RequestReceiver initReceiver(){
        String receiverClassName = ConfigParser.getOrDefault("provider.request-receiver", "conglin.clrpc.transfer.receiver.BasicRequestReceiver");
        RequestReceiver receiver = null;
        try {
            receiver = (RequestReceiver) Class.forName(receiverClassName)
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.receiver.BasicRequestReceiver' rather than "
                    + receiverClassName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.receiver.BasicRequestReceiver}
            if(receiver == null) receiver = new BasicRequestReceiver();
        }
        receiver.init(serviceHandler);
        return receiver;
    }
}
package conglin.clrpc.bootstrap;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.ServerTransfer;
import conglin.clrpc.transfer.net.receiver.BasicRequestReceiver;
import conglin.clrpc.transfer.net.receiver.RequestReceiver;

/**
 * RPC server端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcServerBootstrap bootstrap = new RpcServerBootstrap(); 
 *     bootstrap.addService("service1", ServiceBean1.class)
 *              .addService("service2", new ServiceBean2())
 *              .addService(Interface3.class, Implement3.class)
 *              .start();
 * </pre></blockquote>
 * 
 * 注意：若服务接口相同，先添加的服务会被覆盖。
 *      结束后不要忘记关闭服务端，释放资源。
 */

public class RpcServerBootstrap {

    private static final Logger log = LoggerFactory.getLogger(RpcServerBootstrap.class);

    // 管理传输
    private ServerTransfer serverTransfer;

    // 管理服务
    private ServerServiceHandler serviceHandler;


    public RpcServerBootstrap() {
        serviceHandler = new ServerServiceHandler();
        serverTransfer = new ServerTransfer();
    }

    /**
     * 保存即将发布的服务
     * @param interfaceClass 接口类
     * @param serviceBeanClass 实现类
     * @return
     */
    public RpcServerBootstrap addService(Class<?> interfaceClass, Class<?> serviceBeanClass) {
        if (!interfaceClass.isAssignableFrom(serviceBeanClass)) {
            log.error(serviceBeanClass.getSimpleName() + " is not permitted. And it will not be added Services");
            return this;
        } else {
            return addService(interfaceClass.getSimpleName(), serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceBeanClass 类名必须满足 'xxxServiceImpl' 条件
     * @return
     */
    public RpcServerBootstrap addService(Class<?> serviceBeanClass) {
        String serviceBeanClassName = serviceBeanClass.getSimpleName();
        if (!serviceBeanClassName.endsWith("ServiceImpl")){
            log.error(serviceBeanClassName + " is not permitted. And you must use 'xxxServiceImpl' format classname.");
            return this;
        }else{
            return addService(serviceBeanClassName.substring(0, serviceBeanClassName.length()-4), serviceBeanClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param serviceBeanClass
     * @return
     */
    public RpcServerBootstrap addService(String serviceName, Class<?> serviceBeanClass) {
        if (serviceBeanClass.isInterface()) {
            log.error(serviceBeanClass.getSimpleName() + " is not a service class. And it will not be added Services");
            return this;
        }else {
            serviceHandler.addService(serviceName, serviceBeanClass);
            return this;
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param serviceBean
     * @return
     */
    public RpcServerBootstrap addService(String serviceName, Object serviceBean){
        serviceHandler.addService(serviceName, serviceBean);
        return this;
    }

    /**
     * 移除已经发布的服务
     * @param interfaceClass
     * @return
     */
    public RpcServerBootstrap removeService(Class<?> interfaceClass) {
        return removeService(interfaceClass.getSimpleName());
    }

    /**
     * 移除已经发布的服务
     * @param serviceName
     * @return
     */
    public RpcServerBootstrap removeService(String serviceName) {
        serviceHandler.removeService(serviceName);
        return this;
    }

    /**
     * 启动
     */
    public void start() {
        // 启动Netty并将其注册到zookeeper中
        serviceHandler.start();
        serverTransfer.start(initReceiver(), serviceHandler::registerService);

        // 关闭jvm时 调用stop方法
        // Runtime.getRuntime().addShutdownHook(new Thread(()->{
        //     this.stop();
        // }));
    }

    /**
     * 关闭
     */
    public void stop() {
        serverTransfer.stop();
        serviceHandler.stop();
    }

    /**
     * 获取一个 {@link RequestReceiver} 
     * 并调用 {@link RequestReceiver#init(ServerServiceHandler)} 进行初始化
     * @return
     */
    protected RequestReceiver initReceiver(){
        String receiverClassName = ConfigParser.getOrDefault("server.request-receiver", "conglin.clrpc.transfer.net.receiver.BasicRequestReceiver");
        RequestReceiver receiver = null;
        try {
            receiver = (RequestReceiver) Class.forName(receiverClassName)
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.net.receiver.BasicRequestReceiver' rather than "
                    + receiverClassName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.net.receiver.BasicRequestReceiver}
            receiver = (receiver == null) ? new BasicRequestReceiver() : receiver;
        }
        receiver.init(serviceHandler);
        return receiver;
    }

}
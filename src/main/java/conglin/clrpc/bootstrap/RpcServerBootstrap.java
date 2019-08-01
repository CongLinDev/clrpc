package conglin.clrpc.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.ServerTransfer;

/**
 * RPC server端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcServerBootstrap bootstrap = new RpcServerBootstrap(); 
 *     bootstrap.addService(Interface1.class, Implement1.class)
 *              .addService(Interface2.class, Implement2.class)
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
        serverTransfer.start(serviceHandler);
    }

    /**
     * 关闭
     */
    public void stop() {
        serverTransfer.stop();
        serviceHandler.stop();
    }

}
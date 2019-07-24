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
        serverTransfer = new ServerTransfer();
        serviceHandler = new ServerServiceHandler();
    }

    /**
     * 保存即将发布的服务
     * @param interfaceClass 接口类
     * @param implementClass 实现类
     * @return
     */
    public RpcServerBootstrap addService(Class<?> interfaceClass, Class<?> implementClass) {
        if (!interfaceClass.isAssignableFrom(implementClass)) {
            log.error(implementClass.getSimpleName() + " is not permitted. And it will not be added Services");
            return this;
        } else {
            return addService(interfaceClass.getSimpleName(), implementClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param implementClass 类名必须满足 'xxxServiceImpl' 条件
     * @return
     */
    public RpcServerBootstrap addService(Class<?> implementClass) {
        String implementClassName = implementClass.getSimpleName();
        if (!implementClassName.endsWith("ServiceImpl")){
            log.error(implementClassName + " is not permitted. And you must use 'xxxServiceImpl' format classname.");
            return this;
        }else{
            return addService(implementClassName.substring(0, implementClassName.length()-4), implementClass);
        }
    }

    /**
     * 保存即将发布的服务
     * @param serviceName
     * @param implementClass
     * @return
     */
    public RpcServerBootstrap addService(String serviceName, Class<?> implementClass) {
        if (implementClass.isInterface()) {
            log.error(implementClass.getSimpleName() + " is not a service class. And it will not be added Services");
            return this;
        }else {
            serviceHandler.addService(serviceName, implementClass);
            return this;
        }
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
package conglin.clrpc.bootstrap;

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
        serviceHandler.addService(interfaceClass, implementClass);
        return this;
    }

    /**
     * 保存即将发布的服务
     * @param interfaceClassName
     * @param implementClass
     * @return
     */
    public RpcServerBootstrap addService(String interfaceClassName, Class<?> implementClass) {
        serviceHandler.addService(interfaceClassName, implementClass);
        return this;
    }

    /**
     * 保存即将发布的服务
     * @param implementClass 类名必须满足 'xxxServiceImpl' 条件
     * @return
     */
    public RpcServerBootstrap addService(Class<?> implementClass) {
        serviceHandler.addService(implementClass);
        return this;
    }

    /**
     * 移除已经发布的服务
     * @param interfaceClass
     * @return
     */
    public RpcServerBootstrap removeService(Class<?> interfaceClass) {
        serviceHandler.removeService(interfaceClass);;
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
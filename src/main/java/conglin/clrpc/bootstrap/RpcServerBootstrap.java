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
     * 
     * @param interfaceClass 接口类
     * @param implementClass 实现类
     * @return
     */
    public RpcServerBootstrap addService(Class<?> interfaceClass, Class<?> implementClass) {
        serviceHandler.addService(interfaceClass, implementClass);
        return this;
    }

    /**
     * 扫描并保存所有使用 {@link conglin.clrpc.service.RpcService} 注解的服务
     * 未实现
     */
    public RpcServerBootstrap findService(){
        serviceHandler.findService();
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
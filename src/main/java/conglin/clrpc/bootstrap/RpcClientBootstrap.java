package conglin.clrpc.bootstrap;

import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.net.ClientTransfer;

/**
 * RPC client端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcClientBootstrap bootstrap = new RpcClientBootstrap();
 *     bootstrap.start();
 *     //同步服务
 *     Interface1 i1 = bootstrap.getService("service1");
 *     Interface2 i2 = bootstrap.getService(Interface2.class);
 *     //异步服务
 *     ObjectProxy proxy = bootstrap.getAsynchronousService("service3");
 *     
 * </pre></blockquote>
 * 
 * 注意：结束后不要忘记关闭客户端，释放资源。
 */

public class RpcClientBootstrap {

    private ClientTransfer clientTransfer;

    private ClientServiceHandler serviceHandler;

    public RpcClientBootstrap(){
        serviceHandler = new ClientServiceHandler();
        clientTransfer = new ClientTransfer();
    }

    /**
     * 获取同步服务代理
     * @param <T>
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public <T> T getService(Class<T> interfaceClass){
        return getService(interfaceClass, interfaceClass.getSimpleName());
    }

    /**
     * 获取同步服务代理
     * @param <T>
     * @param interfaceClass 接口类
     * @param serviceName 服务名
     * @return 返回代理服务类
     */
    public <T> T getService(Class<T> interfaceClass, String serviceName){
        clientTransfer.findService(serviceName);
        return serviceHandler.getService(interfaceClass, serviceName, clientTransfer.getSender());
    }

    /**
     * 获取异步服务代理
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public ObjectProxy getAsynchronousService(Class<?> interfaceClass){
        return getAsynchronousService(interfaceClass.getSimpleName());
    }

    /**
     * 获取异步服务代理
     * @param serviceName 返回代理服务类
     * @return 返回代理服务类
     */
    public ObjectProxy getAsynchronousService(String serviceName){
        clientTransfer.findService(serviceName);
        return serviceHandler.getAsynchronousService(serviceName, clientTransfer.getSender());
    }

    /**
     * 启动
     */
    public void start(){
        clientTransfer.start(serviceHandler);
        // RequestSender 在 ClientTransfer#start() 方法中构建
        // 注意不要交换两个顺序
        serviceHandler.start(clientTransfer.getSender());
    }

    /**
     * 停止
     */
    public void stop() throws InterruptedException{
        serviceHandler.stop();
        clientTransfer.stop();
    }
}
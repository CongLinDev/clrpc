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
 *     Interface1 i1 = bootstrap.getService(Interface1.class);
 *     Interface2 i2 = bootstrap.getService(Interface2.class);
 *     //异步服务
 *     ObjectProxy proxy = bootstrap.getAsynchronousService(Interface3.class);
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
     * 获取同步服务
     * @param <T>
     * @param interfaceClass 接口类
     * @return 返回代理服务类
     */
    public <T> T getService(Class<T> interfaceClass){
        clientTransfer.findService(interfaceClass);
        return serviceHandler.getService(interfaceClass);
    }

    /**
     * 获取异步服务
     * @param <T>
     * @param interfaceClass
     * @return
     */
    public <T> ObjectProxy getAsynchronousService(Class<T> interfaceClass){
        clientTransfer.findService(interfaceClass);
        return serviceHandler.getAsynchronousService(interfaceClass);
    }

    /**
     * 启动
     */
    public void start(){
        clientTransfer.start(serviceHandler);
        serviceHandler.start(clientTransfer);
    }

    /**
     * 启动
     * 已知服务端地址，不通过zookeeper获取服务
     * @param initRemoteAddress
     */
    public void start(String ...initRemoteAddress){
        clientTransfer.start(serviceHandler, initRemoteAddress);
        serviceHandler.start(clientTransfer);
    }

    /**
     * 停止
     */
    public void stop() throws InterruptedException{
        serviceHandler.stop();
        clientTransfer.stop();
    }
}
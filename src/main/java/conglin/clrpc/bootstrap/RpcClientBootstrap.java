package conglin.clrpc.bootstrap;

import java.util.List;

import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.ClientTransfer;

/**
 * RPC client端启动类
 * 
 * 使用如下代码启动
 * <blockquote><pre>
 *     RpcClientBootstrap bootstrap = new RpcClientBootstrap();
 *     bootstrap.start();
 *     Interface1 i1 = bootstrap.getService(Interface1.class);
 *     Interface2 i2 = bootstrap.getService(Interface2.class);
 *     
 * </pre></blockquote>
 * 
 * 注意：使用完后记得调用 stop()
 */

public class RpcClientBootstrap {

    private ClientTransfer clientTransfer;

    private ClientServiceHandler serviceHandler;

    public RpcClientBootstrap(){
        serviceHandler = new ClientServiceHandler();
        clientTransfer = new ClientTransfer();
    }

    public <T> T getService(Class<T> interfaceClass){
        return serviceHandler.getService(interfaceClass);
    }

    public void start(){
        clientTransfer.start(serviceHandler);
        serviceHandler.start(clientTransfer);
    }

    public void start(List<String> initRemoteAddress){
        clientTransfer.start(serviceHandler, initRemoteAddress);
        serviceHandler.start(clientTransfer);
    }

    public void stop() throws InterruptedException{
        serviceHandler.stop();
        clientTransfer.stop();
    }
}
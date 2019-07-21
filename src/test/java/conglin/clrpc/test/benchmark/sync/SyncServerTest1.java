package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcServerBootstrap;
import conglin.clrpc.test.service.HelloService;
import conglin.clrpc.test.service.impl.HelloServiceImpl;

/**
 * 测试同步调用服务
 */
public class SyncServerTest1 {

    public static void main(String[] args) {
        RpcServerBootstrap serverBootstrap = new RpcServerBootstrap();
        try {
            System.out.println("Server opening...");
            serverBootstrap.addService(HelloService.class, HelloServiceImpl.class)
                    .start();
            
        }finally{
            serverBootstrap.stop();
            System.out.println("Server closing...");
        }
    }
}
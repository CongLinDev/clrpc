package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.HelloService;
import conglin.clrpc.test.service.impl.HelloServiceImpl;

/**
 * 测试同步调用服务
 */
public class SyncProviderTest1 {

    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            bootstrap.publishService(HelloService.class, HelloServiceImpl.class)
                    .start();
            
        }finally{
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}
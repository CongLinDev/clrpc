package conglin.clrpc.test.benchmark.async;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.HelloServiceImpl;

public class AsyncProviderTimeTest {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            bootstrap.publish("HelloService", new HelloServiceImpl())
                    .start();
            
        }finally{
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}
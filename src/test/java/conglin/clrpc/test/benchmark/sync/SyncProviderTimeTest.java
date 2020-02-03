package conglin.clrpc.test.benchmark.sync;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.HelloServiceImpl;

public class SyncProviderTimeTest {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            bootstrap.publish("HelloService", new HelloServiceImpl()).start();

        } finally {
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}
package conglin.clrpc.test.benchmark.transaction;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.HelloServiceImpl;
import conglin.clrpc.test.service.impl.UserServiceImpl;

public class ProviderTest {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            bootstrap.publish("HelloService", new HelloServiceImpl())
                    .publish("UserService", new UserServiceImpl())
                    .start();
        } finally {
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}
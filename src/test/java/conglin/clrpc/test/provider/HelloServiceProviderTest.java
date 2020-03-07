package conglin.clrpc.test.provider;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.HelloServiceImpl;

public class HelloServiceProviderTest {

    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        System.out.println("Provider opening...");
        bootstrap.publish("HelloService", new HelloServiceImpl()).hookStop().start();
    }
}
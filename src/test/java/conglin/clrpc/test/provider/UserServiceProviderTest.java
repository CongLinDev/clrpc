package conglin.clrpc.test.provider;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.UserServiceImpl;

public class UserServiceProviderTest {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        System.out.println("Provider opening...");
        bootstrap.publish("UserService", new UserServiceImpl()).hookStop().start();
    }
}
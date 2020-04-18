package conglin.clrpc.test;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.EchoServiceImpl;
import conglin.clrpc.test.service.impl.FileServiceImpl;
import conglin.clrpc.test.service.impl.HelloServiceImpl;
import conglin.clrpc.test.service.impl.UserServiceImpl;

public class ProviderTest {

    public static void main(String[] args) {
        echoServiceTest();
    }

    protected static void echoServiceTest() {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        bootstrap.publish(new EchoServiceImpl()).hookStop().start();
    }

    protected static void helloServiceTest() {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        bootstrap.publish(new HelloServiceImpl()).hookStop().start();
    }

    protected static void userServiceTest() {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        bootstrap.publish(new UserServiceImpl()).hookStop().start();
    }

    protected static void fileServiceTest() {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        bootstrap.publish(new FileServiceImpl()).hookStop().start();
    }

    protected static void transactionTest() {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        bootstrap.publish(new HelloServiceImpl()).publish(new UserServiceImpl()).hookStop().start();
    }
}
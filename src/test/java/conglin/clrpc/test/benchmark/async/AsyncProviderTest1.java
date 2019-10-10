package conglin.clrpc.test.benchmark.async;

import conglin.clrpc.bootstrap.RpcProviderBootstrap;
import conglin.clrpc.test.service.impl.UserServiceImpl;

/**
 * 测试异步调用服务
 */
public class AsyncProviderTest1 {
    public static void main(String[] args) {
        RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();
        try {
            System.out.println("Provider opening...");
            
            bootstrap.publish("UserService", UserServiceImpl.class).start();
            
        }finally{
            bootstrap.stop();
            System.out.println("Provider closing...");
        }
    }
}
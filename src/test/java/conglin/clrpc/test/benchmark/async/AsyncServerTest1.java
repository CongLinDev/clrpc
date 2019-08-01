package conglin.clrpc.test.benchmark.async;

import conglin.clrpc.bootstrap.RpcServerBootstrap;
import conglin.clrpc.test.service.impl.UserServiceImpl;

/**
 * 测试异步调用服务
 */
public class AsyncServerTest1 {
    public static void main(String[] args) {
        RpcServerBootstrap serverBootstrap = new RpcServerBootstrap();
        try {
            System.out.println("Server opening...");
            
            serverBootstrap.addService("UserService", UserServiceImpl.class).start();
            
        }finally{
            serverBootstrap.stop();
            System.out.println("Server closing...");
        }
    }
}
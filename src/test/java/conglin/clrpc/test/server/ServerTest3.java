package conglin.clrpc.test.server;

import conglin.clrpc.bootstrap.RpcServerBootstrap;
import conglin.clrpc.test.service.UserService;
import conglin.clrpc.test.service.impl.UserServiceImpl;

/**
 * 测试异步调用服务
 */
public class ServerTest3 {
    public static void main(String[] args) {
        RpcServerBootstrap serverBootstrap = new RpcServerBootstrap();
        try {
            System.out.println("Server opening...");
            
            serverBootstrap.addService(UserService.class, UserServiceImpl.class).start();
            
        }finally{
            serverBootstrap.stop();
            System.out.println("Server closing...");
        }
    }
}
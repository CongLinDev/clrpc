package conglin.clrpc.test.server;

import conglin.clrpc.bootstrap.RpcServerBootstrap;
import conglin.clrpc.test.service.UserService;
import conglin.clrpc.test.service.impl.UserServiceImpl;

/**
 * 测试未注册服务情况下
 * 同步获取服务
 */
public class ServerTest2 {
    public static void main(String[] args) {
        RpcServerBootstrap serverBootstrap = new RpcServerBootstrap();
        try {
            System.out.println("Server opening...");
            //未添加服务
            serverBootstrap.addService(UserService.class, UserServiceImpl.class).start();
            
        }finally{
            serverBootstrap.stop();
            System.out.println("Server closing...");
        }
    }
}